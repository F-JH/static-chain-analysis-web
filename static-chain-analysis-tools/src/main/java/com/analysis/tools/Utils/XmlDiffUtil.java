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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.analysis.tools.Config.Code.*;

public class XmlDiffUtil {
    Element root;

    public XmlDiffUtil(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
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
        reader.setEntityResolver(new SAXEntityResolver(reader.getEntityResolver()));
        Document oldDocument = reader.read(new File(oldFile));
        Document newDocument = reader.read(new File(newFile));
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
        protected EntityResolver resolver;
        private static final Map<String, String> dtdCache = new ConcurrentHashMap<>();

        public SAXEntityResolver(EntityResolver resolver) {
            if (resolver == null) {
                throw new IllegalArgumentException("EntityResolver cannot be null");
            }
            this.resolver = resolver;
        }

        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            if (!dtdCache.containsKey(systemId)) {
                InputSource inputSource = resolver.resolveEntity(publicId, systemId);
                if (inputSource != null) {
                    if (inputSource.getByteStream() != null) {
                        // Read byte stream and cache it
                        byte[] content = inputSource.getByteStream().readAllBytes();
                        String cachedContent = new String(content);
                        dtdCache.put(systemId, cachedContent);
                    } else if (inputSource.getCharacterStream() != null) {
                        // Read character stream and cache it
                        String cachedContent = new BufferedReader(inputSource.getCharacterStream()).lines().collect(Collectors.joining("\n"));
                        dtdCache.put(systemId, cachedContent);
                    } else if (inputSource.getSystemId() != null) {
                        dtdCache.put(systemId, inputSource.getSystemId());
                    }
                }
            }
            return new InputSource(new StringReader(dtdCache.get(systemId)));
        }
    }
}
