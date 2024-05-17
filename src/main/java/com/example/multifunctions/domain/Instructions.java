package com.example.multifunctions.domain;

import java.util.List;

public class Instructions {
    private List<Content> contents;
    private SystemInstruction system_instruction;

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    public SystemInstruction getSystem_instruction() {
        return system_instruction;
    }

    public void setSystem_instruction(SystemInstruction system_instruction) {
        this.system_instruction = system_instruction;
    }
}
