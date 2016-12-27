package com.chanapp.chanjet.web.reader;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * resources/目录下的xml文件读取工具
 * 
 * @author tds
 *
 */
public final class XmlReader extends BaseReader<Document> {

    private XmlReader(String xmlPath) {
        super(xmlPath);
    }

    public static XmlReader getInstance(String xmlPath) {
        return new XmlReader(xmlPath);
    }

    public NodeList getNodeList(String tag, boolean reload) {
        Document doc = get(reload);
        if (doc == null) {
            return null;
        }
        return doc.getElementsByTagName(tag);
    }

    public NodeList getNodeList(String tag) {
        return getNodeList(tag, false);
    }

    @Override
    protected Document load(InputStream is) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return db.parse(is);
    }
}
