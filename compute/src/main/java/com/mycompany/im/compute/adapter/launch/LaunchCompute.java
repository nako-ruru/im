package com.mycompany.im.compute.adapter.launch;

import com.mycompany.im.compute.domain.ComputeServer;

public class LaunchCompute {

    public static void main(String[] args) throws Exception {
        new ComputeServer().start();
    }

}