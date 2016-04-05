/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jnode.rest;

import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import jnode.orm.ORMManager;
import org.jnode.rest.auth.BasicAuthenticationFilter;
import org.jnode.rest.db.RestUser;
import org.jnode.rest.di.ClassfileDependencyScanner;
import org.jnode.rest.di.Injector;
import org.jnode.rest.route.MainApiRoute;
import spark.Spark;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;

public class Main extends JnodeModule {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private final int port;

    @Inject
    @Named("basicAuthenticationFilter")
    private BasicAuthenticationFilter filter;

    public static void main(String[] args) throws JnodeModuleException {
        Main mainModule = new Main(Main.class.getResource("config-rest.properties").getPath());
        mainModule.startForTest();
    }

    public Main(String configFile) throws JnodeModuleException {
        super(configFile);
        port = getPort();
    }

    private int getPort() throws JnodeModuleException {
        try {
            return Integer.parseInt(properties.getProperty("rest-api-port", "4567"));
        } catch (NumberFormatException e) {
            throw new JnodeModuleException("bad port value", e);
        }
    }

    @Override
    public void start() {
        try {
            startProd();
        } catch (Exception e) {
            LOGGER.l1("fail", e);
        }
    }

    public void startForTest() {
        try {
            startTest();
        } catch (Exception e) {
            LOGGER.l1("fail", e);
        }
    }

    private void startProd() throws JnodeModuleException {
        ORMManager.get(RestUser.class);
        ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
        scanner.scan("org.jnode.rest", "prod-");
        try {
            Injector.inject(this);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new JnodeModuleException(e);
        }

        initSpark();
    }

    private void startTest() throws JnodeModuleException {
        ClassfileDependencyScanner scanner = new ClassfileDependencyScanner();
        scanner.scan("org.jnode.rest", "mock-");
        try {
            Injector.inject(this);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new JnodeModuleException(e);
        }

        initSpark();
    }

    private void initSpark() throws JnodeModuleException {
        Spark.setPort(port);
        Spark.before(filter);
        Spark.post(new MainApiRoute("/api", new DispatcherFactory().create()));
    }

    @Override
    public void handle(IEvent iEvent) {

    }

    public void setFilter(BasicAuthenticationFilter filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "Main{" +
                "port=" + port +
                "} " + super.toString();
    }
}
