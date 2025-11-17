package com.comecome.openfoodfacts.models;

import java.util.List;

public class RuleViolations {
    private String type;
    private List<String> tags;
    private List<String> exclusores;
    private List<String> depends_on;
    private String requires_tag;
    private String violation_code;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getExclusores() { return exclusores; }
    public void setExclusores(List<String> exclusores) { this.exclusores = exclusores; }

    public List<String> getDepends_on() { return depends_on; }
    public void setDepends_on(List<String> depends_on) { this.depends_on = depends_on; }

    public String getRequires_tag() { return requires_tag; }
    public void setRequires_tag(String requires_tag) { this.requires_tag = requires_tag; }

    public String getViolation_code() { return violation_code; }
    public void setViolation_code(String violation_code) { this.violation_code = violation_code; }
}
