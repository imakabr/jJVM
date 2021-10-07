package jvm.parser;

import jvm.JVMType;

public class Field {

    private final String name;
    private final JVMType type;
    private final int flags;
    
    public Field(String name, JVMType type, int flags) {
        this.name = name;
        this.type = type;
        this.flags = flags;
    }

    public String getName() {
        return name;
    }

    public JVMType getType() {
        return type;
    }

    public int getFlags() {
        return flags;
    }

}
