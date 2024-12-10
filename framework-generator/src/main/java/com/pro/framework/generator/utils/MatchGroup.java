package com.pro.framework.generator.utils;

// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class MatchGroup {
    private String groupRegex;  //有带正则表达式符号的串
    private String subStr; //正则表达式匹配的字符(不带表达式符号)
    private Integer subStrStart;
    private Integer subStrEnd;

    public MatchGroup(String groupRegex, String subStr, Integer subStrStart, Integer subStrEnd) {
        this.groupRegex = groupRegex;
        this.subStr = subStr;
        this.subStrStart = subStrStart;
        this.subStrEnd = subStrEnd;
    }

    public String getGroupRegex() {
        return groupRegex;
    }

    public void setGroupRegex(String groupRegex) {
        this.groupRegex = groupRegex;
    }

    public String getSubStr() {
        return subStr;
    }

    public void setSubStr(String subStr) {
        this.subStr = subStr;
    }

    public Integer getSubStrStart() {
        return subStrStart;
    }

    public void setSubStrStart(Integer subStrStart) {
        this.subStrStart = subStrStart;
    }

    public Integer getSubStrEnd() {
        return subStrEnd;
    }

    public void setSubStrEnd(Integer subStrEnd) {
        this.subStrEnd = subStrEnd;
    }
}
