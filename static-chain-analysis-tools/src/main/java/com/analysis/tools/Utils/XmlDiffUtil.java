package com.analysis.tools.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.analysis.tools.Config.Code.*;

public class XmlDiffUtil {
    Element root;

    public XmlDiffUtil(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new SAXEntityResolver(file));
        Document document = reader.read(file);
        root = document.getRootElement();
    }

    /**
     * 只记录有修改的，新增、删除的不记录，因为这种在代码层面已经会被记录
     * @param oldFile
     * @param newFile
     * @throws DocumentException
     */
    public static List<String> compireXml(String oldFile, String newFile) throws DocumentException {
        SAXReader reader = new SAXReader();
        File oldFileObj = new File(oldFile);
        File newFileObj = new File(newFile);
        reader.setEntityResolver(new SAXEntityResolver(newFileObj));
        Document oldDocument = reader.read(oldFileObj);
        Document newDocument = reader.read(newFileObj);
        Element oldRoot = oldDocument.getRootElement();
        Element newRoot = newDocument.getRootElement();
        String className = newRoot.attribute("namespace").getStringValue();
        className = className.replace(".", PACKAGE_SPLIT);
        List<String> result = new ArrayList<>();

        Iterator<Element> oldIterator = oldRoot.elementIterator();
        Iterator<Element> newIterator = newRoot.elementIterator();

        Map<String, JSONObject> oldCURD = new HashMap<>();
        Map<String, JSONObject> newCURD = new HashMap<>();

        while(oldIterator.hasNext()){
            Element child = (Element) oldIterator.next();
            if(FilterUtils.isCURD(child.getName())){
                JSONObject info = getElementInfo(child);
                oldCURD.put(info.getString("id"), info);
            }
        }
        while(newIterator.hasNext()){
            Element child = (Element) newIterator.next();
            if(FilterUtils.isCURD(child.getName())){
                JSONObject info = getElementInfo(child);
                newCURD.put(info.getString("id"), info);
            }
        }
        for(String id:newCURD.keySet()){
            if(
                oldCURD.keySet().contains(id)
                    && !oldCURD.get(id).toJSONString().equals(newCURD.get(id).toJSONString())
            ){
                result.add(className + METHOD_SPLIT + id);
            }
        }
        return result;
    }

    public static JSONObject getElementInfo(Element element){
        JSONObject result = new JSONObject();
        result.put("tagName", element.getName());
        result.put("innerText", element.getStringValue());
        List<Attribute> attributeList = element.attributes();
        for(Attribute attribute:attributeList){
            result.put(attribute.getName(), attribute.getValue());
        }

        JSONArray children = new JSONArray();

        Stack<ElementNode> recursion = new Stack<>();
        Iterator<Element> iterator = element.elementIterator();
        while(iterator.hasNext()){
            recursion.push(new ElementNode((Element) iterator.next(), children));
        }

        while(!recursion.empty()){
            ElementNode topItem = recursion.pop();
            JSONObject descriptor = new JSONObject();
            descriptor.put("tagName", topItem.element.getName());
            List<Attribute> tmpAttributeList = topItem.element.attributes();
            for(Attribute attribute:tmpAttributeList){
                descriptor.put(attribute.getName(), attribute.getValue());
            }
            Iterator<Element> tmpIterator = topItem.element.elementIterator();
            JSONArray tmpArray = new JSONArray();
            while(tmpIterator.hasNext()){
                recursion.push(new ElementNode((Element) tmpIterator.next(), tmpArray));
            }
            descriptor.put("children", tmpArray);
            topItem.json.add(descriptor);
        }
        result.put("children", children);
        return result;
    }

    public boolean isMapper(){
        return root.getName().equals("mapper");
    }
    public String getMapperNameSpace(){
        return root.attribute("namespace").getValue();
    }

    private static class ElementNode{
        private Element element;
        private JSONArray json;
        private ElementNode(Element element, JSONArray json){
            this.element = element;
            this.json = json;
        }
    }

    protected static class SAXEntityResolver implements EntityResolver, Serializable {
        private String uriPrefix;
        private static final Map<String, String> dtdCache = new ConcurrentHashMap<>();

        public SAXEntityResolver(File file) {
            String path = file.getAbsolutePath();
            if (path != null) {
                StringBuffer sb = new StringBuffer("file://");
                if (!path.startsWith(File.separator)) {
                    sb.append("/");
                }

                path = path.replace('\\', '/');
                sb.append(path);
                String systemId = sb.toString();
                if (systemId != null && systemId.length() > 0) {
                    int idx = systemId.lastIndexOf(47);
                    if (idx > 0) {
                        uriPrefix = systemId.substring(0, idx + 1);
                    }
                }
            }
        }

        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (!dtdCache.containsKey(systemId)) {
                String path = systemId;
                if (uriPrefix != null && systemId.indexOf(58) <= 0) {
                    path = uriPrefix + systemId;
                }
                if (path.startsWith("http")) {
                    try (InputStream inputStream = new URL(path).openStream()) {
                        // 如果dtd是http，则保存下来
                        byte[] content = inputStream.readAllBytes();
                        String cachedContent = new String(content);
                        dtdCache.put(systemId, cachedContent);
                    } catch (IOException e) {
                        throw new SAXException("Failed to read DTD from URL: " + systemId, e);
                    }
                }else{
                    return new InputSource(path);
                }
            }
            InputStream dtdStream = new ByteArrayInputStream(dtdCache.get(systemId).getBytes());
            return new InputSource(dtdStream);
        }
    }
}
