package com.ijimu.capital;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 关键词匹配工具类
 * @author cjx, qqd
 * @since  2010-7-22
 */
public class BlackKeyword implements Serializable {

    private static Log log = LogFactory.getLog(BlackKeyword.class);

    public static boolean DEBUG = false;

    private List<String> keywords = null;
    /** 基于hash的多模匹配算法: hash算法初始化表(by 王维) */
    Map<String, BlackWordHashNode> firstCharHash = null;

    /** 构造函数 */
    public BlackKeyword(List<String> keywords){
        if(keywords !=null) setWords(keywords);
    }
    public BlackKeyword(){
    }

    /** 重新设置关键词 */
    public void setWords(List<String> keywords){
        this.keywords = keywords;
        initPattern();
    }

    /**
     * 添加关键词
     * @param words
     */
    public void addWords(String words) {
        this.keywords.add(words);
        initPattern();
    }

    //## -----------------------  private section  -----------------------------
    /** 初始化 */
    @SuppressWarnings("unchecked")
    private void initPattern() {
        try{
            initTable(this.keywords);
            System.out.printf("INFO: BlackKeyword.initPattern() GOT keywords:%d\n", this.keywords.size());
        } catch (Exception e) {
            log.error(e);
        }
    }

    /** 初始化“快速匹配算法”需要的首字符hash表 */
    private boolean initTable(List<String> keys) {
        long startTime = System.currentTimeMillis();//开始时间
        boolean isMultipleKeyWord = false;
        firstCharHash = new HashMap(4096);
        BlackWordHashNode node = null;
        String firstChar = null;
        try {
            for(String keyword : keys) {
                //读取关键字列表内容
                if (keyword.length()==0){
                    continue;
                }
                firstChar = keyword.substring(0, 1);//取字符串第一个字
                BlackWordHashNode newNode = new BlackWordHashNode();
                newNode.setText(keyword);
                newNode.setNext(null);
                if ((node = firstCharHash.get(firstChar)) != null){//已包含此字
                    while( node.getNext()!=null ){
                        if (keyword.equals(node.getText())){//链表中已包含此关键字
                            isMultipleKeyWord = true;
                            break;
                        }
                        node = node.getNext();
                    }
                    if(isMultipleKeyWord){//链表中已包含此关键字
                        isMultipleKeyWord = false;
                        continue;
                    }
                    node.setNext(newNode);
                }else{
                    firstCharHash.put(firstChar, newNode);
                }
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        long endTime = System.currentTimeMillis();//结束时间
        System.out.println("DEBUG: initMap-Cost:" + (endTime-startTime) + "ms"); //耗时
        return true;
    }


    static class BlackWordHashNode implements Serializable {
        private String text=null;//节点字符串
        private BlackWordHashNode next=null;//下一节点
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public BlackWordHashNode getNext() {
            return next;
        }
        public void setNext(BlackWordHashNode next) {
            this.next = next;
        }
        public String toString() {
            return "node(" + text + ")";
        }
    }

    /** 检验是否包含有某种关键词
     * @return null: 没命中任何关键词
     *        某字符串：命中的第一个关键词
     */
    public String check(String src) {
        return check1(src);
    }

    /** 原始算法 */
    private String check0(String src){
        if(keywords == null) return null;
        try{
            for(String word : keywords) {
                if (word == null || word.length() < 1)
                    continue;
                if (src.indexOf(word) >= 0) {
                    if(DEBUG) System.out.println("Hit BlackKeyword: " + word);
                    return word;
                }
            }
        }catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    /** hash快速匹配算法 */
    private String check1(String artical){//检查输入的文章
        if(firstCharHash == null) return null;
        try{
            int count = 0, srclen = artical.length();
            String index = null; //当前指向的文章中的字符
            String keyword = null; //当前关键字
            String subStringOfArtical = null; //文章片断
            BlackWordHashNode node = null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1);//取这个字：可以优化——寻找能获取一个字符的方法
                //index1=artical.charAt(count);
                if ((node = firstCharHash.get(index))==null){//没有相应的键值
                    count++;//下一个字
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        if (count+keyword.length()<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            subStringOfArtical=artical.substring(count, count+keyword.length());//按关键字取长：可以优化——初始化时排序
                        }else {
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
                            return keyword;//找到关键字并返回
                        }
                    }
                    count++;//下一个字
                }
            }
        }catch (Exception e) {
        }
        return null;
    }

    /**
     * 检查输入的文章是否有禁词，有的直接用*替换
     */
    public String checkAndReplace(String artical){
        return checkAndReplace(artical, "*");
    }
    /**
     * 检查输入的文章是否有禁词，有的直接用replacement替换
     */
    public String checkAndReplace(String artical,String replacement){
        if(firstCharHash == null) {
            return null;
        }
        try{
            int count=0,srclen=artical.length(),keywordLen=0,innerCount=0,replaceLen=replacement.length();
            String index=null/*当前指向的文章中的字符*/,keyword=null/*当前关键字*/,subStringOfArtical=null/*文章片断*/;
            StringBuffer replace=null;
            BlackWordHashNode node=null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1);//取这个字：可以优化——寻找能获取一个字符的方法
                if ((node = firstCharHash.get(index))==null){//没有相应的键值
                    count++;//下一个字
                    innerCount++;
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        keywordLen=keyword.length();
                        if (count+keywordLen<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            subStringOfArtical=artical.substring(count, count+keywordLen);//按关键字取长：可以优化——初始化时排序
                        }else{
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
                            if (replace==null){
                                replace=new StringBuffer();
                                replace.append(artical);
                            }
                            if (replaceLen==1){//替换词为一个字的时候
                                String firstPartOfArtical=replace.substring(0, innerCount);
                                String lastPartOfArtical=replace.substring(innerCount+keywordLen);
                                replace.setLength(0);
                                replace.append(firstPartOfArtical);
                                for (int i=0;i<keywordLen;i++){
                                    replace.append(replacement);
                                }
                                replace.append(lastPartOfArtical);
                                count+=keywordLen-1;
                                innerCount+=keywordLen-1;
                            }else if (replaceLen==0){//替换词为空的时候
                                replace.delete(innerCount, innerCount+keywordLen);
                                count+=keywordLen-1;
                                innerCount--;
                            }else{//替换词为字符串的时候
                                String firstPartOfArtical=replace.substring(0, innerCount);
                                String lastPartOfArtical=replace.substring(innerCount+keywordLen);
                                replace.setLength(0);
                                replace.append(firstPartOfArtical);
                                replace.append(replacement);
                                replace.append(lastPartOfArtical);
                                count+=keywordLen-1;
                                innerCount+=replaceLen-1;
                            }
                            break;
                        }
                    }
                    count++;//下一个字
                    innerCount++;
                }
            }
            if (replace!=null){
                return replace.toString();
            }else{
                return artical;
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public String checkAndReplaceFilter(String artical,String pre,String end) {
        if(firstCharHash == null) return null;

        if(pre == null) pre = "";
        if(end == null) end = "";

        try{
            int count=0,srclen=artical.length(),keywordLen=0,innerCount=0;
            String index=null/*当前指向的文章中的字符*/,keyword=null/*当前关键字*/,subStringOfArtical=null/*文章片断*/;
            StringBuffer replace=null;
            BlackWordHashNode node=null;

            while(count<srclen) {
                index=artical.substring(count, count+1);
                boolean isValid = isValidName(index) < 0;
                if(isValid || (node = firstCharHash.get(index)) == null) {//没有相应的值
                    count++;//下一个字
                    innerCount++;
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        keywordLen=keyword.length();

                        int filNum = 0;
                        if (count+keywordLen<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            StringBuffer tmpStr = new StringBuffer();
                            for(int i=count; i<artical.length(); i++) {
                                String tmpChar = String.valueOf(artical.charAt(i));
                                if(isValidName(tmpChar) < 0) {
                                    filNum ++;
                                    continue;
                                }
                                tmpStr.append(tmpChar);
                                if(tmpStr.length() >= keywordLen) break;
                            }
                            subStringOfArtical = tmpStr.toString();
                            if(subStringOfArtical.length() < keywordLen) {
                                node=node.getNext();//下一个关键字
                                continue;
                            }
//                            subStringOfArtical=artical.substring(count, count+keywordLen);//按关键字取长：可以优化——初始化时排序
                        }else{
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
                            if (replace==null){
                                replace=new StringBuffer();
                                replace.append(artical);
                            }

                            //替换词为字符串的时候
                            String firstPartOfArtical=replace.substring(0, innerCount);
                            String lastPartOfArtical=replace.substring(innerCount+keywordLen+filNum);
                            String oldKeyWord = replace.substring(innerCount,innerCount+keywordLen+filNum);
                            replace.setLength(0);
                            replace.append(firstPartOfArtical);
                            replace.append(pre);
//                       replace.append(keyword);
                            replace.append(oldKeyWord);
                            replace.append(end);
                            replace.append(lastPartOfArtical);
                            count+=keywordLen-1+filNum;
                            innerCount+=pre.length()+keyword.length()+end.length()-1+filNum;
                            break;
                        }
                    }
                    count++;//下一个字
                    innerCount++;
                }
            }

            if (replace!=null){
                return replace.toString();
            }else{
                return artical;
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * 检查输入的文章是否有禁词，有的在前后添加pre 和 end
     */
    public String checkAndReplace(String artical,String pre,String end){
        if(firstCharHash == null) {
            return null;
        }
        if(pre==null) pre = "";
        if(end==null) end = "";
        try{
            int count=0,srclen=artical.length(),keywordLen=0,innerCount=0;
            String index=null/*当前指向的文章中的字符*/,keyword=null/*当前关键字*/,subStringOfArtical=null/*文章片断*/;
            StringBuffer replace=null;
            BlackWordHashNode node=null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1);//取这个字：可以优化——寻找能获取一个字符的方法
                if ((node = firstCharHash.get(index))==null){//没有相应的键值
                    count++;//下一个字
                    innerCount++;
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        keywordLen=keyword.length();
                        if (count+keywordLen<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            subStringOfArtical=artical.substring(count, count+keywordLen);//按关键字取长：可以优化——初始化时排序
                        }else{
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
                            if (replace==null){
                                replace=new StringBuffer();
                                replace.append(artical);
                            }

                            //替换词为字符串的时候
                            String firstPartOfArtical=replace.substring(0, innerCount);
                            String lastPartOfArtical=replace.substring(innerCount+keywordLen);
                            replace.setLength(0);
                            replace.append(firstPartOfArtical);
                            replace.append(pre);
                            replace.append(keyword);
                            replace.append(end);
                            replace.append(lastPartOfArtical);
                            count+=keywordLen-1;
                            innerCount+=pre.length()+keyword.length()+end.length()-1;
                            break;
                        }
                    }
                    count++;//下一个字
                    innerCount++;
                }
            }
            if (replace!=null){
                return replace.toString();
            }else{
                return artical;
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * 检查输入的文章是否有禁词，有的在前后添加pre 和 end
     */
    public String checkAndReplaceNew(String artical,String pre,String end){
        if(firstCharHash == null) {
            return null;
        }
        if(pre==null) pre = "";
        if(end==null) end = "";
        try{
            int count=0,srclen=artical.length(),keywordLen=0,innerCount=0;
            String index=null/*当前指向的文章中的字符*/,keyword=null/*当前关键字*/,subStringOfArtical=null/*文章片断*/;
            StringBuffer replace=null;
            BlackWordHashNode node=null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1).toLowerCase();//取这个字：可以优化——寻找能获取一个字符的方法
                boolean has = (node=firstCharHash.get(index)) != null ? true:(node=firstCharHash.get((index=index.toUpperCase())))==null;
                if (!has){//没有相应的键值
                    count++;//下一个字
                    innerCount++;
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        keywordLen=keyword.length();
                        if (count+keywordLen<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            subStringOfArtical=artical.substring(count, count+keywordLen);//按关键字取长：可以优化——初始化时排序
                        }else{
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equalsIgnoreCase(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
                            if (replace==null){
                                replace=new StringBuffer();
                                replace.append(artical);
                            }

                            //替换词为字符串的时候
                            String firstPartOfArtical=replace.substring(0, innerCount);
                            String lastPartOfArtical=replace.substring(innerCount+keywordLen);
                            replace.setLength(0);
                            replace.append(firstPartOfArtical);
                            replace.append(pre);
                            replace.append(subStringOfArtical);
                            replace.append(end);
                            replace.append(lastPartOfArtical);
                            count+=keywordLen-1;
                            innerCount+=pre.length()+subStringOfArtical.length()+end.length()-1;
                            break;
                        }
                    }
                    count++;//下一个字
                    innerCount++;
                }
            }
            if (replace!=null){
                return replace.toString();
            }else{
                return artical;
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * 统计文章中含有的禁词。
     */
    public Map<String,Integer> checkAndCount(String artical){
        Map<String,Integer> ret = null;
        if(firstCharHash == null) {
            return ret;
        }
        ret = new HashMap<String,Integer>();
        try{
            int count = 0, srclen = artical.length();
            String index = null; //当前指向的文章中的字符
            String keyword = null; //当前关键字
            String subStringOfArtical = null; //文章片断
            BlackWordHashNode node = null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1);//取这个字：可以优化——寻找能获取一个字符的方法
                //index1=artical.charAt(count);
                if ((node = firstCharHash.get(index))==null){//没有相应的键值
                    count++;//下一个字
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        if (count+keyword.length()<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            subStringOfArtical=artical.substring(count, count+keyword.length());//按关键字取长：可以优化——初始化时排序
                        }else {
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
//                            return keyword;//找到关键字并返回
                            count = count + keyword.length() - 1;
                            if(ret.containsKey(keyword)) ret.put(keyword, ret.get(keyword)+1);
                            else ret.put(keyword, 1);
                            break;
                        }
                    }
                    count++;//下一个字
                }
            }
        }catch (Exception e) {
        }
        return ret;
    }

    /** hash快速匹配算法 查找出所有的禁词*/
    public List<String> check2(String artical){//检查输入的文章
        List<String> ret = new ArrayList<String>();
        if(firstCharHash == null) return ret;
        try{
            int count = 0, srclen = artical.length();
            String index = null; //当前指向的文章中的字符
            String keyword = null; //当前关键字
            String subStringOfArtical = null; //文章片断
            BlackWordHashNode node = null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1);//取这个字：可以优化——寻找能获取一个字符的方法
                //index1=artical.charAt(count);
                if ((node = firstCharHash.get(index))==null){//没有相应的键值
                    count++;//下一个字
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        if (count+keyword.length()<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            subStringOfArtical=artical.substring(count, count+keyword.length());//按关键字取长：可以优化——初始化时排序
                        }else {
                            node=node.getNext();//下一个关键字
                            continue;
                        }
                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
//                            return keyword;//找到关键字并返回
                            if(!ret.contains(keyword)) ret.add(keyword);
                            count = count + keyword.length() - 1;
                            break;
                        }
                    }
                    count++;//下一个字
                }
            }
        }catch (Exception e) {
        }
        return ret;
    }




    /** hash快速匹配算法 */
    public String checkF(String artical){//检查输入的文章
        if(firstCharHash == null) return null;
        try{
            int count = 0, srclen = artical.length(),keywordLen = 0;
            String index = null; //当前指向的文章中的字符
            String keyword = null; //当前关键字
            String subStringOfArtical = null; //文章片断
            BlackWordHashNode node = null;
            while(count<srclen) {//对第count个字
                index=artical.substring(count, count+1);//取这个字：可以优化——寻找能获取一个字符的方法
                boolean isValid = isValidName(index) < 0;
                //index1=artical.charAt(count);
                if (isValid || (node = firstCharHash.get(index))==null){//没有相应的键值
                    count++;//下一个字
                    continue;
                }else{
                    while (node!=null){
                        keyword=node.getText();//关键字
                        keywordLen=keyword.length();
                        int filNum = 0;
                        if (count+keyword.length()<=srclen){//避免超出字符串长度范围：可以优化——换成try、catch
                            StringBuffer tmpStr = new StringBuffer();
                            for(int i=count; i<artical.length(); i++) {
                                String tmpChar = String.valueOf(artical.charAt(i));
                                if(isValidName(tmpChar) < 0) {
                                    filNum ++;
                                    continue;
                                }
                                tmpStr.append(tmpChar);
                                if(tmpStr.length() >= keywordLen) break;
                            }
                            subStringOfArtical = tmpStr.toString();
                            if(subStringOfArtical.length() < keywordLen) {
                                node=node.getNext();//下一个关键字
                                continue;
                            }
//                            subStringOfArtical=artical.substring(count, count+keyword.length());//按关键字取长：可以优化——初始化时排序
                            count = count + keywordLen;
                        }else {
                            node=node.getNext();//下一个关键字
                            continue;
                        }

                        if (!keyword.equals(subStringOfArtical)){//节点匹配不成功
                            node=node.getNext();//下一个关键字
                            continue;
                        }else{
                            return keyword;//找到关键字并返回
                        }
                    }
                    count++;//下一个字
                }
            }
        }catch (Exception e) {
        }
        return null;
    }


    private static int isValidName(String src) {
        if(src == null || src.length() <= 0) return -1;
        for(int i=0; i<src.length(); i++) {
            //str.substring(i, i+1).matches("[\\u4e00-\\u9fa5]+")
            char cur = src.charAt(i);
            //System.out.println("vn " + i + "\tchar=" + cur + "\tint=" + (int)cur);
            if(cur < '0') return -2;
            if(cur > '9' && cur < 'A') return -3;
            if(cur > 'Z' && cur < 'a') return -4;
            if(cur > 'z' && cur < 0x4e00) return -5;
            if(cur > 0x9fa5 ) return -6;
        }
        return 0;
    }

}