package com.intellectualsites.download;

import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.util.RequestManager;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.implementation.DefaultLogWrapper;
import xyz.kvantum.server.implementation.ServerContext;
import xyz.kvantum.server.implementation.StandaloneServer;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

public final class QuickStart {

    public static Kvantum newStandaloneServer(Object... classes) throws xyz.kvantum.server.implementation.QuickStart.ServerStartFailureException {
        ServerContext
            kvantumContext = ServerContext.builder().coreFolder(new File("./kvantum")).logWrapper(new DefaultLogWrapper()).router(
            RequestManager.builder().build()).standalone(true).serverSupplier(StandaloneServer::new).build();
        Optional<Kvantum> kvantumOptional = kvantumContext.create();
        if (!kvantumOptional.isPresent()) {
            throw new ServerStartFailureException(new IllegalStateException("Failed to create server instance"));
        } else {
            Kvantum kvantum = (Kvantum)kvantumOptional.get();
            Object[] var4 = classes;
            int var5 = classes.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Object object = var4[var6];
                if (object == null) {
                    throw new NullPointerException("Passed object is null. Not suitable for routing.");
                }

                if (object instanceof RequestHandler) {
                    kvantum.getRouter().add((RequestHandler)object);
                } else {
                    Collection<? extends RequestHandler> added = kvantum.getRouter().scanAndAdd(object);
                    if (added.isEmpty()) {
                        throw new IllegalArgumentException("No views declarations found in " + object);
                    }
                }
            }

            return kvantum;
        }
    }

    private QuickStart() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final class ServerStartFailureException extends RuntimeException {
        ServerStartFailureException(Exception e) {
            super(e);
        }

        ServerStartFailureException() {
        }
    }
}
