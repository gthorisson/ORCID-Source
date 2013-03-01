/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2013 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.03 at 11:47:41 AM BST 
//


package org.orcid.jaxb.model.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}url-name" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}url" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.orcid.org/ns/orcid}visibility"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "urlName",
    "url"
})
@XmlRootElement(name = "researcher-url")
public class ResearcherUrl implements Comparable<ResearcherUrl>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8978502528841038967L;
    @XmlElement(name = "url-name")
    protected UrlName urlName;
    protected Url url;
      

    public ResearcherUrl() {
        super();
    }
    
    public ResearcherUrl(Url url) {
        super();
        this.url = url;
    }

    /**
     * Gets the value of the urlName property.
     * 
     * @return
     *     possible object is
     *     {@link UrlName }
     *     
     */
    public UrlName getUrlName() {
        return urlName;
    }

    /**
     * Sets the value of the urlName property.
     * 
     * @param value
     *     allowed object is
     *     {@link UrlName }
     *     
     */
    public void setUrlName(UrlName value) {
        this.urlName = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link Url }
     *     
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link Url }
     *     
     */
    public void setUrl(Url value) {
        this.url = value;
    }

    @Override
    public int compareTo(ResearcherUrl researcherUrl) {        
        if (researcherUrl == null || StringUtils.isBlank(researcherUrl.getUrl().getValue())) {
            return -1;
        } else if (url == null || StringUtils.isBlank(url.getValue())) {
            return 1;
        } else {
            return url.compareTo(researcherUrl.getUrl());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResearcherUrl)) {
            return false;
        }

        ResearcherUrl that = (ResearcherUrl) o;

        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        if (urlName != null ? !urlName.equals(that.urlName) : that.urlName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = urlName != null ? urlName.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}