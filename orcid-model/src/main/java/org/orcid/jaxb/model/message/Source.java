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
// Generated on: 2012.08.09 at 01:52:56 PM BST 
//


package org.orcid.jaxb.model.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}source-orcid" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}source-name" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}source-date" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sourceOrcid",
    "sourceName",
    "sourceDate"
})
@XmlRootElement(name = "source")
public class Source implements Serializable {

    @XmlElement(name = "source-orcid")
    protected SourceOrcid sourceOrcid;
    @XmlElement(name = "source-name")
    protected SourceName sourceName;
    @XmlElement(name = "source-date")
    protected SourceDate sourceDate;

    /**
     * Gets the value of the sourceOrcid property.
     * 
     * @return
     *     possible object is
     *     {@link SourceOrcid }
     *     
     */
    public SourceOrcid getSourceOrcid() {
        return sourceOrcid;
    }

    /**
     * Sets the value of the sourceOrcid property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceOrcid }
     *     
     */
    public void setSourceOrcid(SourceOrcid value) {
        this.sourceOrcid = value;
    }

    /**
     * Gets the value of the sourceName property.
     * 
     * @return
     *     possible object is
     *     {@link SourceName }
     *     
     */
    public SourceName getSourceName() {
        return sourceName;
    }

    /**
     * Sets the value of the sourceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceName }
     *     
     */
    public void setSourceName(SourceName value) {
        this.sourceName = value;
    }

    /**
     * Gets the value of the sourceDate property.
     * 
     * @return
     *     possible object is
     *     {@link SourceDate }
     *     
     */
    public SourceDate getSourceDate() {
        return sourceDate;
    }

    /**
     * Sets the value of the sourceDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceDate }
     *     
     */
    public void setSourceDate(SourceDate value) {
        this.sourceDate = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceDate == null) ? 0 : sourceDate.hashCode());
        result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
        result = prime * result + ((sourceOrcid == null) ? 0 : sourceOrcid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Source other = (Source) obj;
        if (sourceDate == null) {
            if (other.sourceDate != null)
                return false;
        } else if (!sourceDate.equals(other.sourceDate))
            return false;
        if (sourceName == null) {
            if (other.sourceName != null)
                return false;
        } else if (!sourceName.equals(other.sourceName))
            return false;
        if (sourceOrcid == null) {
            if (other.sourceOrcid != null)
                return false;
        } else if (!sourceOrcid.equals(other.sourceOrcid))
            return false;
        return true;
    }

}