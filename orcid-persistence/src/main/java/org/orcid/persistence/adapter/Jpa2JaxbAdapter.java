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
package org.orcid.persistence.adapter;

import org.orcid.jaxb.model.clientgroup.OrcidClientGroup;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.persistence.jpa.entities.ProfileEntity;

/**
 * orcid-persistence - Dec 7, 2011 - Jaxb2JpaAdapter
 * 
 * @author Declan Newman (declan)
 **/

public interface Jpa2JaxbAdapter {

    OrcidProfile toOrcidProfile(ProfileEntity profileEntity);

    OrcidClientGroup toOrcidClientGroup(ProfileEntity profileEntity);

}
