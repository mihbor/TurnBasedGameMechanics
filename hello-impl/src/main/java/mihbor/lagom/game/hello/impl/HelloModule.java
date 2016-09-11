/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package mihbor.lagom.game.hello.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import mihbor.lagom.game.api.GameService;
import mihbor.lagom.game.hello.api.HelloService;
import mihbor.lagom.game.impl.GameServiceImpl;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindServices(serviceBinding(HelloService.class, HelloServiceImpl.class),
    	serviceBinding(GameService.class, GameServiceImpl.class));
  }
}
