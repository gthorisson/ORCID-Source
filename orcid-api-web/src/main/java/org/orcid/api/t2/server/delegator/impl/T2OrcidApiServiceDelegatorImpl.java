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
package org.orcid.api.t2.server.delegator.impl;

import static org.orcid.api.common.OrcidApiConstants.PROFILE_GET_PATH;
import static org.orcid.api.common.OrcidApiConstants.STATUS_OK_MESSAGE;
import static org.orcid.api.common.OrcidApiConstants.WORKS_PATH;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.orcid.api.common.delegator.OrcidApiServiceDelegator;
import org.orcid.api.common.exception.OrcidBadRequestException;
import org.orcid.api.common.exception.OrcidNotFoundException;
import org.orcid.api.common.validation.ValidOrcidMessage;
import org.orcid.api.t2.server.delegator.T2OrcidApiServiceDelegator;
import org.orcid.core.manager.OrcidProfileManager;
import org.orcid.core.manager.OrcidSearchManager;
import org.orcid.core.manager.ValidationManager;
import org.orcid.core.security.visibility.aop.AccessControl;
import org.orcid.core.security.visibility.aop.VisibilityControl;
import org.orcid.jaxb.model.message.CreationMethod;
import org.orcid.jaxb.model.message.OrcidHistory;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidSearchResult;
import org.orcid.jaxb.model.message.OrcidSearchResults;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.jaxb.model.message.Source;
import org.orcid.jaxb.model.message.SourceName;
import org.orcid.jaxb.model.message.SourceOrcid;
import org.orcid.jaxb.model.message.SubmissionDate;
import org.orcid.utils.DateUtils;
import org.orcid.utils.NullUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

/**
 * 2011-2012 ORCID
 * <p/>
 * The delegator for the tier 2 API.
 * <p/>
 * The T2 delegator is responsible for the validation, retrieving results and
 * passing of objects to be from the core
 * 
 * @author Declan Newman (declan) Date: 07/03/2012
 */
@Component("orcidT2ServiceDelegator")
public class T2OrcidApiServiceDelegatorImpl implements T2OrcidApiServiceDelegator {

    @Resource(name = "orcidProfileManager")
    private OrcidProfileManager orcidProfileManager;

    @Resource(name = "orcidSearchManager")
    private OrcidSearchManager orcidSearchManager;

    @Resource
    private ValidationManager validationManager;

    @Override
    public Response viewStatusText() {
        return Response.ok(STATUS_OK_MESSAGE).build();
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the profile's bio
     * details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_BIO_READ_LIMITED)
    public Response findBioDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedOrcidBio(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the profile's
     * external identifier details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_BIO_READ_LIMITED)
    public Response findExternalIdentifiers(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedExternalIdentifiers(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with all of the profile's
     * details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_PROFILE_READ_LIMITED)
    public Response findFullDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedOrcidProfile(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    /**
     * Takes the {@link org.orcid.jaxb.model.message.OrcidMessage} and attempts
     * to update the bio details only. If there is content other than the bio in
     * the message, it should return a 400 Bad Request. Privilege checks will be
     * performed to determine if the client or user has permissions to perform
     * the update.
     * 
     * @param orcidMessage
     *            the message containing the bio to be updated. Any elements
     *            outside of the bio will cause a 400 Bad Request to be returned
     * @return if the update was successful, a 200 response will be returned
     *         with the updated
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_BIO_UPDATE)
    @ValidOrcidMessage
    public Response updateBioDetails(UriInfo uriInfo, String orcid, OrcidMessage orcidMessage) {
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        try {
            orcidProfile = orcidProfileManager.updateOrcidBio(orcidProfile);
            if (orcidProfile != null) {
                orcidProfile.setOrcidActivities(null);
            }
            return getOrcidMessageResponse(orcidProfile, orcid);
        } catch (DataAccessException e) {
            throw new OrcidBadRequestException("Cannot create ORCID");
        }
    }

    /**
     * finds and returns the {@link org.orcid.jaxb.model.message.OrcidMessage}
     * wrapped in a {@link javax.xml.ws.Response} with only the work details
     * 
     * @param orcid
     *            the ORCID to be used to identify the record
     * @return the {@link javax.xml.ws.Response} with the
     *         {@link org.orcid.jaxb.model.message.OrcidMessage} within it
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_WORKS_READ_LIMITED)
    public Response findWorksDetails(String orcid) {
        OrcidProfile profile = orcidProfileManager.retrieveClaimedOrcidWorks(orcid);
        return getOrcidMessageResponse(profile, orcid);
    }

    /**
     * Creates a new profile and returns the saved representation of it. The
     * response should include the 'location' to retrieve the newly created
     * profile from.
     * 
     * @param orcidMessage
     *            the message to be saved. If the message already contains an
     *            ORCID value a 400 Bad Request
     * @return if the creation was successful, returns a 201 along with the
     *         location of the newly created resource otherwise returns an error
     *         response describing the problem
     */
    @Override
    @ValidOrcidMessage
    @AccessControl(requiredScope = ScopePathType.ORCID_PROFILE_CREATE)
    public Response createProfile(UriInfo uriInfo, OrcidMessage orcidMessage) {
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        checkHasAtLeastOneEmail(orcidProfile);
        try {
            setSponsorFromAuthentication(orcidProfile);
            orcidProfile = orcidProfileManager.createOrcidProfileAndNotify(orcidProfile);
            return getCreatedResponse(uriInfo, PROFILE_GET_PATH, orcidProfile);
        } catch (DataAccessException e) {
            throw new OrcidBadRequestException("Cannot create ORCID", e);
        }
    }

    private void checkHasAtLeastOneEmail(OrcidProfile orcidProfile) {
        if (NullUtils.anyNull(orcidProfile.getOrcidBio(), orcidProfile.getOrcidBio().getContactDetails())
                || orcidProfile.getOrcidBio().getContactDetails().getEmail().isEmpty()) {
            throw new OrcidBadRequestException("There must be a least one email in the new profile");
        }
    }

    /**
     * Add works to an existing ORCID profile. If the profile already contains
     * an identifiable work with the same id a 409 Conflict should be returned.
     * 
     * @param orcidMessage
     *            the message containing the works to be added
     * @return if the works were all added successfully, a 201 with a location
     *         should be returned
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_WORKS_CREATE)
    @ValidOrcidMessage
    public Response addWorks(UriInfo uriInfo, String orcid, OrcidMessage orcidMessage) {
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        try {
            orcidProfile = orcidProfileManager.addOrcidWorks(orcidProfile);
            if (orcidProfile != null) {
                orcidProfile.setOrcidBio(null);
            }
            return getCreatedResponse(uriInfo, WORKS_PATH, orcidProfile);
        } catch (DataAccessException e) {
            throw new OrcidBadRequestException("Cannot create ORCID");
        }
    }

    /**
     * Update the works for a given ORCID profile. This will cause all content
     * to be overwritten
     * 
     * @param orcidMessage
     *            the message containing all works to overwritten. If any other
     *            elements outside of the works are present, a 400 Bad Request
     *            is returned
     * @return
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_WORKS_UPDATE)
    @ValidOrcidMessage
    public Response updateWorks(UriInfo uriInfo, String orcid, OrcidMessage orcidMessage) {
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        try {
            orcidProfile = orcidProfileManager.updateOrcidWorks(orcidProfile);
            return getOrcidMessageResponse(orcidProfile, orcid);
        } catch (DataAccessException e) {
            throw new OrcidBadRequestException("Cannot create ORCID");
        }
    }

    /**
     * Add new external identifiers to the profile. As with all calls, if the
     * message contains any other elements, a 400 Bad Request will be returned.
     * 
     * @param orcidMessage
     *            the message congtaining the external ids
     * @return If successful, returns a 200 OK with the updated content.
     */
    @Override
    @AccessControl(requiredScope = ScopePathType.ORCID_BIO_EXTERNAL_IDENTIFIERS_CREATE)
    @ValidOrcidMessage
    public Response addExternalIdentifiers(UriInfo uriInfo, String orcid, OrcidMessage orcidMessage) {
        OrcidProfile orcidProfile = orcidMessage.getOrcidProfile();
        try {
            orcidProfile = orcidProfileManager.addExternalIdentifiers(orcidProfile);
            return getOrcidMessageResponse(orcidProfile, orcid);
        } catch (DataAccessException e) {
            throw new OrcidBadRequestException("Cannot create ORCID");
        }
    }

    @Override
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    public Response deleteProfile(UriInfo uriInfo, String orcid) {
        try {
            OrcidProfile deletedProfile = orcidProfileManager.deleteProfile(orcid);
            return getOrcidMessageResponse(deletedProfile, orcid);
        } catch (DataAccessException e) {
            throw new OrcidBadRequestException("Cannot delete ORCID");
        }
    }

    private Response getCreatedResponse(UriInfo uriInfo, String requested, OrcidProfile profile) {
        if (profile != null && profile.getOrcid() != null && StringUtils.isNotBlank(profile.getOrcid().getValue())) {
            URI uri = uriInfo.getBaseUriBuilder().path("/").path(requested).build(profile.getOrcid().getValue());
            return Response.created(uri).build();
        } else {
            throw new OrcidNotFoundException("Cannot find ORCID");
        }
    }

    /**
     * Method to perform the mundane task of checking for null and returning the
     * response with an OrcidMessage entity
     * 
     * @param profile
     * @param requestedOrcid
     * @return
     */
    private Response getOrcidMessageResponse(OrcidProfile profile, String requestedOrcid) {
        if (profile != null) {
            OrcidMessage orcidMessage = new OrcidMessage(profile);
            return Response.ok(orcidMessage).build();
        } else {
            throw new OrcidNotFoundException("ORCID " + requestedOrcid + " not found");
        }
    }

    /**
     * See {@link OrcidApiServiceDelegator}{@link #searchByQuery(Map)}
     */
    @Override
    @VisibilityControl
    public Response searchByQuery(Map<String, List<String>> queryMap) {
        OrcidMessage orcidMessage = orcidSearchManager.findOrcidsByQuery(queryMap);
        List<OrcidSearchResult> searchResults = orcidMessage.getOrcidSearchResults() != null ? orcidMessage.getOrcidSearchResults().getOrcidSearchResult() : null;
        List<OrcidSearchResult> filteredResults = new ArrayList<OrcidSearchResult>();
        if (searchResults != null && searchResults.size() > 0) {
            for (OrcidSearchResult searchResult : searchResults) {
                OrcidSearchResult filteredSearchResult = new OrcidSearchResult();
                filteredSearchResult.setRelevancyScore(searchResult.getRelevancyScore());
                OrcidProfile filteredProfile = new OrcidProfile();
                String retrievedOrcid = searchResult.getOrcidProfile().getOrcid().getValue();
                filteredProfile.setOrcid(retrievedOrcid);
                filteredProfile.setOrcidBio(searchResult.getOrcidProfile().getOrcidBio());
                filteredSearchResult.setOrcidProfile(filteredProfile);
                filteredResults.add(filteredSearchResult);
            }

        }

        OrcidSearchResults orcidSearchResults = new OrcidSearchResults();
        orcidSearchResults.getOrcidSearchResult().addAll(filteredResults);
        return getOrcidSearchResultsResponse(orcidSearchResults, queryMap.toString());

    }

    private Response getOrcidSearchResultsResponse(OrcidSearchResults orcidSearchResults, String query) {
        if (orcidSearchResults != null) {
            OrcidMessage orcidMessage = new OrcidMessage();
            orcidMessage.setOrcidSearchResults(orcidSearchResults);
            validationManager.validateMessage(orcidMessage);
            return Response.ok(orcidMessage).build();
        } else {
            throw new OrcidNotFoundException("No search results found using query " + query);
        }
    }

    public void setSponsorFromAuthentication(OrcidProfile profile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (profile.getOrcidHistory() == null) {
            OrcidHistory orcidHistory = new OrcidHistory();
            orcidHistory.setCreationMethod(CreationMethod.API);
            profile.setOrcidHistory(orcidHistory);
        }
        profile.getOrcidHistory().setSubmissionDate(new SubmissionDate(DateUtils.convertToXMLGregorianCalendar(new Date())));
        if (OAuth2Authentication.class.isAssignableFrom(authentication.getClass())) {
            AuthorizationRequest authorizationRequest = ((OAuth2Authentication) authentication).getAuthorizationRequest();
            Source sponsor = new Source();
            String sponsorOrcid = authorizationRequest.getClientId();
            OrcidProfile sponsorProfile = orcidProfileManager.retrieveOrcidProfile(sponsorOrcid);
            sponsor.setSourceName(new SourceName(sponsorProfile.getOrcidBio().getPersonalDetails().getCreditName().getContent()));
            sponsor.setSourceOrcid(new SourceOrcid(sponsorOrcid));
            profile.getOrcidHistory().setSource(sponsor);
        }
    }

}