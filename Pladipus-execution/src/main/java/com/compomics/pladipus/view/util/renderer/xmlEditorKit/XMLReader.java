package com.compomics.pladipus.view.util.renderer.xmlEditorKit;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This package was adapted for our use from http://www.edankert.com/bounce/xmleditorkit.html
 * 
 */
public class XMLReader {
    static XMLReader instance=new XMLReader();
    private XMLReader() {
        
    }
    public static XMLReader getInstance() {
        return instance;
    }
    public void read(InputStream is, Document d, int pos) throws IOException, BadLocationException{
        if (!(d instanceof XMLDocument)) {
            return;
        }


        XMLDocument doc=(XMLDocument)d;
        doc.setUserChanges(false);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setCoalescing(true);
        dbf.setValidating (false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(false);


        try {
            //Using factory get an instance of document builder
            javax.xml.parsers.DocumentBuilder dbXML = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            org.w3c.dom.Document dom = dbXML.parse(is);

            ArrayList<DefaultStyledDocument.ElementSpec> specs=new ArrayList<DefaultStyledDocument.ElementSpec>();
            DefaultStyledDocument.ElementSpec spec=new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(), DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);

//            printNode(dom, "");
            if (doc.getLength()==0) {
                writeNode(doc,dom, pos, specs);
            }
            else {
                writeNode(doc,dom.getDocumentElement(), pos, specs);
            }

            DefaultStyledDocument.ElementSpec[] data = new DefaultStyledDocument.ElementSpec[specs.size()];
            specs.toArray(data);
            doc.insert(pos, data);
            
            doc.setUserChanges(true);
        } catch(SAXException pce) {
            pce.printStackTrace();
            throw new IOException(pce.getMessage());
        } catch(ParserConfigurationException pce) {
            pce.printStackTrace();
            throw new IOException(pce.getMessage());
        } catch(IOException pce) {
            pce.printStackTrace();
            throw new IOException(pce.getMessage());
        }
    }

    public int writeNode(Document doc, Node node, int pos, ArrayList<DefaultStyledDocument.ElementSpec> specs)  throws BadLocationException{
        SimpleAttributeSet tagAttrs=new SimpleAttributeSet();
        tagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.TAG_ELEMENT);
        SimpleAttributeSet tagRowStartAttrs=new SimpleAttributeSet();
        tagRowStartAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.TAG_ROW_START_ELEMENT);
        SimpleAttributeSet tagRowEndAttrs=new SimpleAttributeSet();
        tagRowEndAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.TAG_ROW_END_ELEMENT);

        DefaultStyledDocument.ElementSpec spec;
        spec=new DefaultStyledDocument.ElementSpec(tagAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
        specs.add(spec);
        spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
        specs.add(spec);

        int offs=pos;
//        <
        spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"<".toCharArray(), 0, 1);
        specs.add(spec);

//        tag name
        if (node instanceof org.w3c.dom.Document && doc.getLength()==0) {
            org.w3c.dom.Document dd=(org.w3c.dom.Document)node;
            String nodeStr="?xml version=\""+dd.getXmlVersion()+"\" encoding=\""+dd.getXmlEncoding()+"\"?";

            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    nodeStr.toCharArray(), 0, nodeStr.length());
        }
        else {
            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    node.getNodeName().toCharArray(), 0, node.getNodeName().length());
        }
        specs.add(spec);

        NamedNodeMap attrs=node.getAttributes();
        if (attrs!=null && attrs.getLength()>0) {
            for (int i=0; i<attrs.getLength(); i++) {
                Node attr=attrs.item(i);
                String name=attr.getNodeName();
                String value=attr.getNodeValue();
//                " "
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType," ".toCharArray(), 0, 1);
                specs.add(spec);
//                attr name
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.ATTRIBUTENAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,name.toCharArray(), 0, name.length());
                specs.add(spec);
//                ="
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"=\"".toCharArray(), 0, 2);
                specs.add(spec);
//                attr value
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.ATTRIBUTEVALUE_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,value.toCharArray(), 0, value.length());
                specs.add(spec);
//                "
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"\"".toCharArray(), 0, 1);
                specs.add(spec);
            }
        }

        org.w3c.dom.NodeList list=node.getChildNodes();
        if (list!=null && list.getLength()>0) {
//            >
            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,">\n".toCharArray(), 0, 2);
            specs.add(spec);
            spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);

            for (int i=0; i<list.getLength(); i++) {
                Node n=list.item(i);
                if (n instanceof Element) {
                    Element child=(Element)n;
                    offs+=writeNode(doc, child, offs, specs);
                }
                else if (n.getNodeType()==Node.COMMENT_NODE) {
                    //plain text
                    String str=n.getNodeValue();
                    int ii=str.indexOf("\n");
                    while (ii>0) {
                        spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
                        specs.add(spec);

                        String value=str.substring(0,ii);
                        spec=new DefaultStyledDocument.ElementSpec(XMLDocument.COMMENT_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,value.toCharArray(), 0, value.length());
                        specs.add(spec);
                        spec=new DefaultStyledDocument.ElementSpec(XMLDocument.COMMENT_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"\n".toCharArray(), 0, 1);
                        specs.add(spec);

                        spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
                        specs.add(spec);

                        str=str.substring(ii+1);
                        ii=str.indexOf("\n");
                    }
                    spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
                    specs.add(spec);

                    spec=new DefaultStyledDocument.ElementSpec(XMLDocument.COMMENT_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,str.toCharArray(), 0, str.length());
                    specs.add(spec);
                    spec=new DefaultStyledDocument.ElementSpec(XMLDocument.COMMENT_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"\n".toCharArray(), 0, 1);
                    specs.add(spec);

                    spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
                    specs.add(spec);
                }
                else {
                    //plain text
                    spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
                    specs.add(spec);

                    spec=new DefaultStyledDocument.ElementSpec(XMLDocument.PLAIN_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,n.getNodeValue().toCharArray(), 0, n.getNodeValue().length());
                    specs.add(spec);
                    spec=new DefaultStyledDocument.ElementSpec(XMLDocument.PLAIN_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"\n".toCharArray(), 0, 1);
                    specs.add(spec);

                    spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
                    specs.add(spec);
                }
            }
            spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
            specs.add(spec);
            if (node instanceof org.w3c.dom.Document) {
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType," ".toCharArray(), 0, 1);
                specs.add(spec);
            }
            else {
    //            </
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"</".toCharArray(), 0, 2);
                specs.add(spec);
    //            tag name
                    spec=new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,node.getNodeName().toCharArray(), 0, node.getNodeName().length());
                    specs.add(spec);
    //            />
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,">\n".toCharArray(), 0, 2);
                specs.add(spec);
            }
            spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }
        else {
//            />
            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"/>\n".toCharArray(), 0, 3);
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(), DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }

        spec=new DefaultStyledDocument.ElementSpec(tagAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
        specs.add(spec);

        return offs-pos;
    }
    public int writeNodeOld(Document doc, Element node, int pos)  throws BadLocationException{
        int offs=pos;
        doc.insertString(offs, "<", XMLDocument.BRACKET_ATTRIBUTES);
        offs++;
        doc.insertString(offs, node.getNodeName(), XMLDocument.TAGNAME_ATTRIBUTES);
        offs+=node.getNodeName().length();

        NamedNodeMap attrs=node.getAttributes();
        if (attrs!=null && attrs.getLength()>0) {
            for (int i=0; i<attrs.getLength(); i++) {
                Node attr=attrs.item(i);
                String name=attr.getNodeName();
                String value=attr.getNodeValue();
                doc.insertString(offs, " ", XMLDocument.BRACKET_ATTRIBUTES);
                offs++;
                doc.insertString(offs, name, XMLDocument.ATTRIBUTENAME_ATTRIBUTES);
                offs+=name.length();
                doc.insertString(offs, "=\"", XMLDocument.BRACKET_ATTRIBUTES);
                offs+=2;
                doc.insertString(offs, value, XMLDocument.ATTRIBUTEVALUE_ATTRIBUTES);
                offs+=value.length();
                doc.insertString(offs, "\"", XMLDocument.BRACKET_ATTRIBUTES);
                offs+=1;
            }
        }

        org.w3c.dom.NodeList list=node.getChildNodes();
        if (list!=null && list.getLength()>0) {
            doc.insertString(offs, ">\n", XMLDocument.BRACKET_ATTRIBUTES);
            offs+=2;
            for (int i=0; i<list.getLength(); i++) {
                Node n=list.item(i);
                if (n instanceof Element) {
                    Element child=(Element)n;
                    offs+=writeNodeOld(doc, child, offs);
                }
                else {
                    //plain text
                    doc.insertString(offs, n.getNodeValue()+"\n", XMLDocument.PLAIN_ATTRIBUTES);
                    offs+=n.getNodeValue().length()+1;
                }
            }
            doc.insertString(offs, "<", XMLDocument.BRACKET_ATTRIBUTES);
            offs++;
            doc.insertString(offs, node.getNodeName(), XMLDocument.TAGNAME_ATTRIBUTES);
            offs+=node.getNodeName().length();
            doc.insertString(offs, "/>\n", XMLDocument.BRACKET_ATTRIBUTES);
            offs+=3;
        }
        else {
            doc.insertString(offs, "/>\n", XMLDocument.BRACKET_ATTRIBUTES);
            offs+=3;
        }

        return offs-pos;
    }

    private static void printNode(Node node, String indent)  {

        switch (node.getNodeType()) {

            case Node.DOCUMENT_NODE:
                System.out.println("<xml version=\"1.0\">\n");
                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.getLength(); i++) {
                        printNode(nodes.item(i), "");
                    }
                }
                break;

            case Node.ELEMENT_NODE:
                String name = node.getNodeName();
                System.out.print(indent + "<" + name);
                NamedNodeMap attributes = node.getAttributes();
                for (int i=0; i<attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    System.out.print(
                        " " + current.getNodeName() +
                        "=\"" + current.getNodeValue() +
                        "\"");
                }
                System.out.print(">");

                // recurse on each child
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i=0; i<children.getLength(); i++) {
                        printNode(children.item(i), indent + "  ");
                    }
                }

                System.out.print("</" + name + ">");
                break;

            case Node.TEXT_NODE:
                System.out.print(node.getNodeValue());
                break;
            default:
                System.out.print(node.getNodeValue());
                break;
        }

    }
}
