package com.mycompany.im.compute.adapter.rpc;


import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProduct{
    @Override
    public String getProductName() {
        return "jim";
    }
}