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
package org.orcid.api.t2.server.delegator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.orcid.api.common.exception.OrcidBadRequestException;
import org.orcid.core.oauth.OrcidOAuth2Authentication;
import org.orcid.jaxb.model.message.ContactDetails;
import org.orcid.jaxb.model.message.CreditName;
import org.orcid.jaxb.model.message.Email;
import org.orcid.jaxb.model.message.GivenNames;
import org.orcid.jaxb.model.message.OrcidActivities;
import org.orcid.jaxb.model.message.OrcidBio;
import org.orcid.jaxb.model.message.OrcidMessage;
import org.orcid.jaxb.model.message.OrcidProfile;
import org.orcid.jaxb.model.message.OrcidWorks;
import org.orcid.jaxb.model.message.PersonalDetails;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.jaxb.model.message.Visibility;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.test.DBUnitTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.api.uri.UriBuilderImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-t2-web-context.xml", "classpath:orcid-t2-security-context.xml" })
public class T2OrcidApiServiceDelegatorTest extends DBUnitTest {

    private static final List<String> DATA_FILES = Arrays.asList("/data/SecurityQuestionEntityData.xml", "/data/SubjectEntityData.xml", "/data/ProfileEntityData.xml");

    @Resource
    private T2OrcidApiServiceDelegator t2OrcidApiServiceDelegator;

    @Mock
    private UriInfo mockedUriInfo;

    @BeforeClass
    public static void initDBUnitData() throws Exception {
        initDBUnitData(DATA_FILES, null);
    }

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());
    }

    @After
    public void after() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testFindWorksDetails() {
        setUpSecurityContext();
        Response response = t2OrcidApiServiceDelegator.findWorksDetails("4444-4444-4444-4441");
        assertNotNull(response);
    }

    @Test
    public void testAddWorks() {
        setUpSecurityContext();
        OrcidMessage orcidMessage = new OrcidMessage();
        OrcidProfile orcidProfile = new OrcidProfile();
        orcidMessage.setOrcidProfile(orcidProfile);
        orcidProfile.setOrcid("4444-4444-4444-4441");
        OrcidActivities orcidActivities = new OrcidActivities();
        orcidProfile.setOrcidActivities(orcidActivities);
        OrcidWorks orcidWorks = new OrcidWorks();
        orcidActivities.setOrcidWorks(orcidWorks);
        Response response = t2OrcidApiServiceDelegator.addWorks(mockedUriInfo, "4444-4444-4444-4441", orcidMessage);
        assertNotNull(response);
    }

    @Test(expected = OrcidBadRequestException.class)
    public void testCreateBioWithMultiplePrimaryEmails() {
        setUpSecurityContextForClientOnly();
        OrcidMessage orcidMessage = createStubOrcidMessage();
        ContactDetails contactDetails = orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails();
        List<Email> emailList = new ArrayList<>();
        String[] emailStrings = new String[] { "madeupemail@semantico.com", "madeupemail2@semantico.com" };
        for (String emailString : emailStrings) {
            Email email = new Email(emailString);
            email.setPrimary(true);
            emailList.add(email);
        }
        contactDetails.getEmail().addAll(emailList);

        t2OrcidApiServiceDelegator.createProfile(mockedUriInfo, orcidMessage);
    }

    @Test
    public void testReadClaimedAsClientOnly() {
        setUpSecurityContextForClientOnly();
        String orcid = "4444-4444-4444-4441";

        Response readResponse = t2OrcidApiServiceDelegator.findFullDetails(orcid);

        assertNotNull(readResponse);
        assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
        OrcidMessage retrievedMessage = (OrcidMessage) readResponse.getEntity();
        assertEquals(orcid, retrievedMessage.getOrcidProfile().getOrcid().getValue());
        assertEquals("S. Milligan", retrievedMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getContent());
    }

    @Test
    public void testCreateAndReadOwnCreation() {
        setUpSecurityContextForClientOnly();
        OrcidMessage orcidMessage = createStubOrcidMessage();
        Email email = new Email("madeupemail@semantico.com");
        orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getEmail().add(email);

        Response createResponse = t2OrcidApiServiceDelegator.createProfile(mockedUriInfo, orcidMessage);

        assertNotNull(createResponse);
        assertEquals(HttpStatus.SC_CREATED, createResponse.getStatus());
        String location = ((URI) createResponse.getMetadata().getFirst("Location")).getPath();
        assertNotNull(location);
        String orcid = location.substring(1, 20);

        Response readResponse = t2OrcidApiServiceDelegator.findFullDetails(orcid);
        assertNotNull(readResponse);
        assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
        OrcidMessage retrievedMessage = (OrcidMessage) readResponse.getEntity();
        assertEquals(orcid, retrievedMessage.getOrcidProfile().getOrcid().getValue());
        assertEquals("Test credit name", retrievedMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getCreditName().getContent());
    }

    @Test
    public void testReadUnclaimedWhenNotOwnCreation() {
        setUpSecurityContextForClientOnly();
        OrcidMessage orcidMessage = createStubOrcidMessage();
        Email email = new Email("madeupemail2@semantico.com");
        orcidMessage.getOrcidProfile().getOrcidBio().getContactDetails().getEmail().add(email);

        Response createResponse = t2OrcidApiServiceDelegator.createProfile(mockedUriInfo, orcidMessage);

        assertNotNull(createResponse);
        assertEquals(HttpStatus.SC_CREATED, createResponse.getStatus());
        String location = ((URI) createResponse.getMetadata().getFirst("Location")).getPath();
        assertNotNull(location);
        String orcid = location.substring(1, 20);

        setUpSecurityContextForClientOnly("4444-4444-4444-4448");

        Response readResponse = t2OrcidApiServiceDelegator.findFullDetails(orcid);
        assertNotNull(readResponse);
        assertEquals(HttpStatus.SC_OK, readResponse.getStatus());
        OrcidMessage retrievedMessage = (OrcidMessage) readResponse.getEntity();
        assertEquals(orcid, retrievedMessage.getOrcidProfile().getOrcid().getValue());
        GivenNames givenNames = retrievedMessage.getOrcidProfile().getOrcidBio().getPersonalDetails().getGivenNames();
        assertNotNull(givenNames);
        assertEquals("Reserved For Claim", givenNames.getContent());
    }

    private OrcidMessage createStubOrcidMessage() {
        OrcidMessage orcidMessage = new OrcidMessage();
        OrcidProfile orcidProfile = new OrcidProfile();
        orcidMessage.setOrcidProfile(orcidProfile);
        OrcidBio orcidBio = new OrcidBio();
        orcidProfile.setOrcidBio(orcidBio);
        PersonalDetails personalDetails = new PersonalDetails();
        orcidBio.setPersonalDetails(personalDetails);
        CreditName creditName = new CreditName("Test credit name");
        personalDetails.setCreditName(creditName);
        creditName.setVisibility(Visibility.LIMITED);
        ContactDetails contactDetails = new ContactDetails();
        orcidBio.setContactDetails(contactDetails);
        return orcidMessage;
    }

    private void setUpSecurityContext() {
        SecurityContextImpl securityContext = new SecurityContextImpl();
        OrcidOAuth2Authentication mockedAuthentication = mock(OrcidOAuth2Authentication.class);
        securityContext.setAuthentication(mockedAuthentication);
        SecurityContextHolder.setContext(securityContext);
        when(mockedAuthentication.getPrincipal()).thenReturn(new ProfileEntity("4444-4444-4444-4441"));
        AuthorizationRequest authorizationRequest = mock(AuthorizationRequest.class);
        Set<String> scopes = new HashSet<String>();
        scopes.add(ScopePathType.ORCID_WORKS_CREATE.value());
        when(authorizationRequest.getScope()).thenReturn(scopes);
        when(mockedAuthentication.getAuthorizationRequest()).thenReturn(authorizationRequest);
    }

    private void setUpSecurityContextForClientOnly() {
        setUpSecurityContextForClientOnly("4444-4444-4444-4447");
    }

    private void setUpSecurityContextForClientOnly(String clientId) {
        SecurityContextImpl securityContext = new SecurityContextImpl();
        OrcidOAuth2Authentication mockedAuthentication = mock(OrcidOAuth2Authentication.class);
        securityContext.setAuthentication(mockedAuthentication);
        SecurityContextHolder.setContext(securityContext);
        when(mockedAuthentication.getPrincipal()).thenReturn(new ProfileEntity(clientId));
        when(mockedAuthentication.isClientOnly()).thenReturn(true);
        AuthorizationRequest authorizationRequest = mock(AuthorizationRequest.class);
        when(authorizationRequest.getClientId()).thenReturn(clientId);
        Set<String> scopes = new HashSet<String>();
        scopes.add(ScopePathType.ORCID_PROFILE_CREATE.value());
        when(authorizationRequest.getScope()).thenReturn(scopes);
        when(mockedAuthentication.getAuthorizationRequest()).thenReturn(authorizationRequest);
    }

}
