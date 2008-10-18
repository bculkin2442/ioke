/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang;

import java.util.IdentityHashMap;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class Context extends IokeObject {
    IokeObject ground;

    public IokeObject message;
    public IokeObject surroundingContext;

    public Context(Runtime runtime, IokeObject ground, String documentation, IokeObject message, IokeObject surroundingContext) {
        super(runtime, documentation);
        this.ground = ground.getRealContext();
        this.message = message;
        this.surroundingContext = surroundingContext;
        
        if(runtime.context != null) {
            this.mimics(runtime.context);
        }

        setCell("self", getRealContext());
    }
    
    public void init() {
        setKind("Context");
    }

    public IokeObject getRealContext() {
        return ground;
    }

    public IokeObject findCell(IokeObject m, IokeObject context, String name, IdentityHashMap<IokeObject, Object> visited) {
        IokeObject nn = super.findCell(m, context, name, visited);
        
        if(nn == runtime.nul) {
            return ground.findCell(m, context, name, visited);
        } else {
            return nn;
        }
    }

    @Override
    public String toString() {
        return "Context:" + System.identityHashCode(this) + "<" + ground + ">";
    }
}// Context
