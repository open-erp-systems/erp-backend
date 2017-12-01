package com.jukusoft.erp.core.module.calculator;

import com.jukusoft.erp.lib.message.StatusCode;
import com.jukusoft.erp.lib.module.AbstractModule;

public class CalculatorModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        //add api method for addition
        this.addRoute("add_integer", (req, res) -> {
            int sum = 0;

            //check and get params
            int a = req.getInt("a");
            int b = req.getInt("b");

            //calculate sum
            sum = a + b;

            //set return value and status code 200
            res.setResult(sum);
        });
    }

    @Override
    public void stop() throws Exception {

    }

}
