package com.hsm;

import java.io.Serializable;

public class GameMessage implements Serializable {

    private static Object[] NULL_PARAM = new Object[0];

    private String functionName;
    private Object[] parameters = NULL_PARAM;

    public transient INetSession netsession;

    public GameMessage(){}
    public GameMessage(String functionName, Object[] parameters){
        this.functionName = functionName;
        this.parameters = parameters;
    }
    public GameMessage(String functionName){
        this.functionName = functionName;
    }

    public INetSession getNetsession() {
        return netsession;
    }
    public void setNetsession(INetSession netsession) {
        this.netsession = netsession;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        try {
            StringBuffer sb = new StringBuffer("[" + functionName);
            sb.append("(");
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    if (i != 0){
                        sb.append(",");
                    }
                    try {
                        sb.append(parameters[i]);
                    } catch (Exception e) {
                        sb.append("[error]");
                    }
                }
            }
            sb.append(")]");
            return sb.toString();
        } catch (Exception e) {
            return "[toString of GameMessage fail]" + functionName;
        }
    }
}
