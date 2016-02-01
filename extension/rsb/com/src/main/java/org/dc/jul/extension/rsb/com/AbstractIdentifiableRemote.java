/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.iface.Identifiable;

/**
 *
 * @author Divine Threepwood
 * @param <M>
 */
public abstract class AbstractIdentifiableRemote<M extends GeneratedMessage> extends RSBRemoteService<M> implements Identifiable<String> {

//    @Override
//    public ServiceType getServiceType() {
//        return ServiceType.MULTI;
//    }

    @Override
    public String getId() throws CouldNotPerformException {
        return (String) getField(FIELD_ID);
    }
//
//    @Override
//    @Deprecated
//    public ServiceConfigType.ServiceConfig getServiceConfig() {
//        // TODO mpohling: redesign!
//        throw new UnsupportedOperationException("Not supported yet.");
//
//    }
}
