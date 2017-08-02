package com.mycompany.im.compute.domain;

public class LaunchCompute {

    static {
        /*
        try {
            String path= "/log4j2.xml";
            LoggerContext context =(LoggerContext) LogManager.getContext(false);
            context.setConfigLocation(LaunchCompute.class.getResource(path).toURI());
            context.reconfigure();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/
    }

    public static void main(String[] args) throws Exception {
        new ComputeServer().start();
    }

}