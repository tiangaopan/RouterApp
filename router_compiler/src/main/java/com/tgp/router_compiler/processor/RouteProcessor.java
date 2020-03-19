package com.tgp.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.tgp.router_annotation.Route;
import com.tgp.router_annotation.model.RouteMeta;
import com.tgp.router_compiler.utils.Consts;
import com.tgp.router_compiler.utils.Log;
import com.tgp.router_compiler.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


/**
 * 注解处理器
 * @author 田高攀
 * @since 2020/3/19 2:03 PM
 */
/**
 * 支持的注解
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(Consts.ANN_TYPE_ROUTE)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RouteProcessor extends AbstractProcessor {

    /**
     * key:组名 value:类名
     */
    private Map<String, String> rootMap = new TreeMap<>();
    /**
     * 分组 key:组名 value:对应组的路由信息
     */
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();
    /**
     * 节点工具类 (类、函数、属性都是节点)
     */
    private Elements elementUtils;

    /**
     * type(类信息)工具类
     */
    private Types typeUtils;
    /**
     * 文件生成器 类/资源
     */
    private Filer filerUtils;
    /**
     * 参数
     */
    private String moduleName;

    private Log log;



    /**
     * 初始化 从 {@link ProcessingEnvironment} 中获得一系列处理器工具
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //获得apt的日志输出
        log = Log.newLog(processingEnvironment.getMessager());
        log.i("init()");
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filerUtils = processingEnvironment.getFiler();
        //参数是模块名 为了防止多模块/组件化开发的时候 生成相同的 xx$$ROOT$$文件
        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(Consts.ARGUMENTS_NAME);
        }
        log.i("RouteProcessor Parmaters:" + moduleName);
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set Processor Parmaters.");
        }
    }
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //判断是否添加了注解
        if (!Utils.isEmpty(set)) {
            //获取所有被Route标记的元素集合
            Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(Route.class);
            //处理Route注解
            if (!Utils.isEmpty(elementsAnnotatedWith)) {
                log.i("route class size : " + elementsAnnotatedWith.size());
                try {
                    parseRoutes(elementsAnnotatedWith);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }



        return false;
    }

    private void parseRoutes(Set<? extends Element> elementsAnnotatedWith) throws IOException {
        //通过节点工具，传入全类名，生成节点(activity)
        TypeElement typeElement = elementUtils.getTypeElement(Consts.ACTIVITY);
        //节点的描述信息
        TypeMirror typeMirror1 = typeElement.asType();
        for (Element element : elementsAnnotatedWith) {
            //路由信息
            RouteMeta routeMeta;
            //获取使用注解的类信息
            TypeMirror typeMirror = element.asType();
            //获取到route注解
            Route route = element.getAnnotation(Route.class);
            //route只能配在activity中，判断是否匹配
            if (typeUtils.isSubtype(typeMirror, typeMirror1)) {
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, element);
            } else {
                throw new RuntimeException("不满足条件，当前只能配置在Activity上" + element);
            }
            //分组信息记录  groupMap <Group分组,RouteMeta路由信息> 集合
            //检查是否配置 group 如果没有配置，则从path截取出组名
            categories(routeMeta);
        }
        //生成类需要实现的接口
        TypeElement iRouteGroup = elementUtils.getTypeElement(Consts.IROUTE_GROUP);
        TypeElement iRouteRoot = elementUtils.getTypeElement(Consts.IROUTE_ROOT);

        /**
         * 生成Group类 作用:记录 <地址,RouteMeta路由信息(Class文件等信息)>
         */
        generatedGroup(iRouteGroup);
        /**
         * 生成Root类 作用:记录 <分组，对应的Group类>
         */
        generatedRoot(iRouteRoot, iRouteGroup);
    }

    private void generatedRoot(TypeElement iRouteRoot, TypeElement iRouteGroup) throws IOException {
        //类型 Map<String,Class<? extends IRouteGroup>> routes>
        //Wildcard 通配符
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
                )
        );

        //参数 Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec rootParamSpec = ParameterSpec.builder(routes, "routes")
                .build();
        //函数 public void loadInfo(Map<String,Class<? extends IRouteGroup>> routes> routes)
        MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec.methodBuilder
                (Consts.METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(rootParamSpec);

        //函数体
        for (Map.Entry<String, String> entry : rootMap.entrySet()) {
            loadIntoMethodOfRootBuilder.addStatement("routes.put($S, $T.class)", entry
                    .getKey(), ClassName.get(Consts.PACKAGE_OF_GENERATE_FILE, entry.getValue
                    ()));
        }
        //生成 $Root$类
        String rootClassName = Consts.NAME_OF_ROOT + moduleName;
        JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(rootClassName)
                        .addSuperinterface(ClassName.get(iRouteRoot))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(loadIntoMethodOfRootBuilder.build())
                        .build()
        ).build().writeTo(filerUtils);

        log.i("Generated RouteRoot: " + Consts.PACKAGE_OF_GENERATE_FILE + "." + rootClassName);
    }

    private void generatedGroup(TypeElement iRouteGroup) throws IOException {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ClassName.get(RouteMeta.class));

        //创建参数名字 Map<string ,RouteMeta > typeNameMap
        ParameterSpec spec = ParameterSpec.builder(typeName, "typeNameMap").build();
        //遍历分组，每一个分组创建一个  ..$$Group$$.. 类
        for (Map.Entry<String, List<RouteMeta>> stringListEntry : groupMap.entrySet()) {
            //创建方法
            MethodSpec.Builder builder = MethodSpec.methodBuilder(Consts.METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addParameter(spec);
            String groupName = stringListEntry.getKey();
            List<RouteMeta> groupData = stringListEntry.getValue();
            for (RouteMeta routeMeta : groupData) {
                // 组装函数体:
                // atlas.put(地址,RouteMeta.build(Class,path,group))
                /**
                 * $S $T  $L两个占位符
                 * $S ---->String
                 * $T------>Class类
                 * $L------>字面量
                 */
                // $S https://github.com/square/javapoet#s-for-strings
                // $T https://github.com/square/javapoet#t-for-types
                builder.addStatement(
                        "typeNameMap.put($S, $T.build($T.$L,$T.class, $S, $S))",
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath().toLowerCase(),
                        routeMeta.getGroup().toLowerCase());
            }
            // 创建java文件($$Group$$)  组
            String groupClassName = Consts.NAME_OF_GROUP + groupName;
            JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(groupClassName)
                            .addSuperinterface(ClassName.get(iRouteGroup))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(builder.build())
                            .build()
            ).build().writeTo(filerUtils);
            log.i("Generated RouteGroup: " + Consts.PACKAGE_OF_GENERATE_FILE + "." +
                    groupClassName);
            //分组名和生成的对应的Group类类名
            rootMap.put(groupName, groupClassName);

        }
    }

    private void categories(RouteMeta routeMeta) {
        if (routeVerify(routeMeta)) {
            List<RouteMeta> routeMetas = groupMap.get(routeMeta.getGroup());
            //如果之前未分组，就进行创建，已经分组过的，直接添加到组里就可以了
            if (Utils.isEmpty(routeMetas)) {
                List<RouteMeta> routeMetaList = new ArrayList<>();
                routeMetaList.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetaList);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            log.i("group info error : " + routeMeta.getPath());
        }
    }

    private boolean routeVerify(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        if (Utils.isEmpty(path) || !path.startsWith("/")) {
            return false;
        }
        if (Utils.isEmpty(group)) {
            //   "/main/test" => group = main
            String substring = path.substring(1, path.indexOf("/"));
            if (Utils.isEmpty(substring)) {
                return false;
            }
            routeMeta.setGroup(substring);
            return true;
        }
        return true;
    }
}
