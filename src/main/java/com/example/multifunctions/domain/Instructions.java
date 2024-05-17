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

class Content {
    private String role;
    private List<Part> parts;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }
}

class Part {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

class SystemInstruction {
    private List<Part> parts;

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }
}