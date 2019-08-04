package com.space.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created by lucifel on 19-8-2.
 */
public class MyDispatcherServlet extends HttpServlet {
    //ioc容器，存储所有Controller对象。Key为类名
    private Map<String,Object> iocContainer = new HashMap<String,Object>();
    //保存controller的requestMapping，key为类名
    private Map<String,String> requestMapping_controller = new HashMap<String,String>();
    //保存uri和对应方法-对象
    private Map<String,MyHandlerMapping> handlerMapping = new HashMap<String,MyHandlerMapping>();
    //视图解析器
    private MyViewResolver myViewResolver;


    @Override
    public void init(ServletConfig config) throws ServletException {
        //扫描有MyController的注解类并实例化保存在ioc中
        scanController(config);
        //将iocContainer中controller类中的MyRequestMapping注解的方法存储到handlerMapping中
        initHandlerMapping();
        //根据视图解析器的配置加载解析器
        loadViewResolver(config);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getServletPath();
        MyHandlerMapping mapping_mothod = handlerMapping.get(uri);
        if(mapping_mothod == null){
            resp.sendError(404);
            return;
        }
        Method method = mapping_mothod.getControllerMothod();
        Object controller_class = iocContainer.get(mapping_mothod.getControllerClassName());
        try {
            String return_str = (String) method.invoke(controller_class);
            if(return_str.startsWith("forward:")){
                String uri_forward = return_str.substring(return_str.indexOf(":") + 1);
                req.getRequestDispatcher(uri_forward).forward(req,resp);
            }else if(return_str.startsWith("redirect:")){
                String uri_redirect = return_str.substring(return_str.indexOf(":") + 1);
                resp.sendRedirect(req.getContextPath() + uri_redirect);
            }else{
                String result = myViewResolver.jspMapping(return_str);
                req.getRequestDispatcher(result).forward(req,resp);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    private void scanController(ServletConfig config) {
        try {
            String path = config.getServletContext().getRealPath("") + "/WEB-INF/classes/" + config.getInitParameter("contextConfigLocation");
            InputStream inputStream = new FileInputStream(path);
            //InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("myspringmvc-config.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element rootElement = document.getRootElement();
            Iterator<Element> iterator = rootElement.elementIterator();
            while (iterator.hasNext()){
                Element element = iterator.next();
                if("component-scan".equals(element.getName())){
                    String package_value = element.attributeValue("base-package");
                    List<String> classPathList = getAllClassByPackage(package_value);
                    for (int i = 0; i < classPathList.size(); i++) {
                        Class<?> controller_class = Class.forName(classPathList.get(i));
                        if(controller_class.isAnnotationPresent(MyController.class)){
                            MyRequestMapping myRequestMapping = controller_class.getAnnotation(MyRequestMapping.class);
                            String controller_key = myRequestMapping == null?"":myRequestMapping.value();
                            String className = controller_class.getName();
                            iocContainer.put(className,controller_class.newInstance());
                            requestMapping_controller.put(className,controller_key);
                        }
                    }
                }
            }


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List getAllClassByPackage(String package_value) {
        String class_path = package_value.replace(".","/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(class_path);
        List result = new ArrayList<String>();
        getClassFile(result,url.getPath(),package_value);
        return result;


    }

    private void getClassFile(List result,String path,String basePath){

        File file = new File(path);
        File[] files = file.listFiles();
        if(files != null){
            for (File f:files) {
                if(f.isDirectory()){
                    String base_path = basePath + "." + f.getName();
                    getClassFile(result,f.getAbsolutePath(),base_path);
                }else if(f.getName().endsWith(".class")){
                    result.add(basePath + "." + (f.getName().replace(".class","")));
                }
            }

        }

    }


    private void initHandlerMapping() {
        for (String controller_name:requestMapping_controller.keySet()) {
            Class controller_class = iocContainer.get(controller_name).getClass();
            Method[] methods = controller_class.getMethods();
            for(Method m:methods){
                if(m.isAnnotationPresent(MyRequestMapping.class)){
                    MyRequestMapping annotation = m.getAnnotation(MyRequestMapping.class);
                    String handlerMapping_key = requestMapping_controller.get(controller_name) +
                            annotation.value();
                    MyHandlerMapping handler_mothod = new MyHandlerMapping();
                    handler_mothod.setControllerClassName(controller_name);
                    handler_mothod.setControllerMothod(m);
                    handlerMapping.put(handlerMapping_key,handler_mothod);
                }
            }

        }
    }

    private void loadViewResolver(ServletConfig config) {
        try {

            String path = config.getServletContext().getRealPath("") + "/WEB-INF/classes/" + config.getInitParameter("contextConfigLocation");
            InputStream inputStream = new FileInputStream(path);
            //InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("myspringmvc-config.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element rootElement = document.getRootElement();
            Iterator<Element> iterator = rootElement.elementIterator("bean");
            while (iterator.hasNext()){
                Element element = iterator.next();
                String bean_className = element.attributeValue("class");
                if(MyViewResolver.class.getName().equals(bean_className)){
                    Class<?> viewResolver_class = Class.forName(bean_className);
                    Object viewResolver_obj = viewResolver_class.newInstance();
                    Iterator<Element> property_iter = element.elementIterator("property");
                    while (property_iter.hasNext()){
                        Element property = property_iter.next();
                        String property_name = property.attributeValue("name");
                        String property_value = property.attributeValue("value");
                        String method_name = "set" + property_name.substring(0,1).toUpperCase() + property_name.substring(1);
                        Field field = viewResolver_class.getDeclaredField(property_name);
                        Method declaredMethod = viewResolver_class.getDeclaredMethod(method_name, field.getType());
                        declaredMethod.invoke(viewResolver_obj,property_value);
                    }
                    myViewResolver = (MyViewResolver) viewResolver_obj;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
