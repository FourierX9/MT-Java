package org.mindtrails.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.binding.ObjectExpression;
import org.junit.Assert;
import org.mindtrails.Application;
import org.mindtrails.MockClasses.*;
import org.mindtrails.controller.ExportController;
import org.mindtrails.controller.QuestionController;
import org.mindtrails.controllers.BaseControllerTest;
import org.mindtrails.controllers.ExportControllerTest;
import org.mindtrails.domain.Participant;
import org.mindtrails.domain.Study;
import org.mindtrails.domain.importData.Scale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mindtrails.domain.jsPsych.JsPsychTrial;
import org.mindtrails.domain.tracking.EmailLog;
import org.mindtrails.domain.tracking.GiftLog;
import org.mindtrails.domain.tracking.TaskLog;
import org.mindtrails.persistence.EmailLogRepository;
import org.mindtrails.persistence.JsPsychRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.AssertTrue;

import static junit.framework.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 7/22/14
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Transactional
public class ImportServiceTest extends BaseControllerTest {

    @Autowired
    private ImportService service;
    private String testScale = "JsPsychTrial";

    @Autowired
    private ExportController exportController;
    @Autowired
    private QuestionController questionController;
    @Autowired
    private TestQuestionnaireRepository repo;
    @Autowired
    private TestUndeleteableRepository repoU;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EmailLogRepository emailRepo;
    @Autowired
    private JsPsychRepository jsPsyRepo;



    private static final Logger LOGGER = LoggerFactory.getLogger((ImportServiceTest.class));


    private void createTestEntries() {
        Participant p = new Participant("Joe Test", "j@t.com", false);
        p.setStudy(new TestStudy());
        entityManager.persist(p);
        entityManager.persist(new TestQuestionnaire("MyTestValue",p));
        entityManager.persist(new TestUndeleteable("MyTestValue"));
        entityManager.persist(new GiftLog(p, "Order1", "Session 1"));
        entityManager.persist(new EmailLog(p, "Email1"));
        entityManager.persist(new TaskLog(p.getStudy(), 25.0));
        entityManager.persist(new JsPsychTrial(1,"Mobile",true));
    }


    @Override
    public Object[] getControllers() {
        return (new Object[]{exportController, questionController});
    }


    /**
     * Testing for @DataOnly and @ClientOnly
     * @throws Exception
     */


    @Test
    public void callFunction() throws Exception {
        LOGGER.info("These request should fail:");
        Object answer = service.deleteOnline("",1);
        assertFalse((boolean)answer);
    }

    @Test
    public void getScaleList() throws Exception {
        LOGGER.info("Check point 1");
        List<Scale> list = service.importList("");
        LOGGER.info("Check point 2");
        for (Scale scale : list) {
            LOGGER.info(scale.getName());
        }
        assertNotNull(list);
    }


    @Test
    public void getScale() throws Exception {
        LOGGER.info("Successfully fired getScale()");
        String list = service.getOnline("",testScale);
        LOGGER.info("Successfully read online: " + list);
        assertNotNull(list);
    }


    @Test
    public void getType() throws Exception {
        LOGGER.info("Successfully fired getScaleType");
        Class<?> ans = service.getClass(testScale);
        LOGGER.info("Got it! This is a " + ans + " !");
        assertNotNull(ans);
    }

    @Test
    public void saveScale() throws Exception {
        LOGGER.info("Save scale fired.");
        String is = service.getOnline("",testScale);
        LOGGER.info("Got the data! " + is);
        assertTrue(service.parseDatabase(testScale, is));
    }


    @Test
    public void updateParticipantTableOnline() throws Exception {
        LOGGER.info("Try to update the participant table");
        assertTrue(service.updateParticipantOnline());
    }



    @Test
    public void updateStudyTable() throws Exception {
        LOGGER.info("Try to update the study table");
        assertTrue(service.updateStudyOnline());
    }


    @Test
    public void saveAllLog() throws Exception {
        LOGGER.info("Try to update all the log files");
        int i = 0;
        List<String> good = new ArrayList<String>();
        List<String> bad = new ArrayList<String>();
        List<Scale> list = service.importList("");
        for (Scale scale : list) {
            if (scale.getName().toLowerCase().contains("log")) {
                String is = service.getOnline("",scale.getName());
                if (service.parseDatabase(scale.getName(), is)) {
                    i += 1;
                    good.add(scale.getName());
                } else {
                    bad.add(scale.getName());
                }
            }
        }
        LOGGER.info("Here is the good list:");
        for (String flag : good) LOGGER.info(flag);
        LOGGER.info("Here is the bad list:");
        for (String flag : bad) LOGGER.info(flag);
        assertEquals(i, list.size());
    }


    @Test
    public void saveAllScale() throws Exception {
        LOGGER.info("Save All Scale!!!!");
        int i = 0;
        List<String> good = new ArrayList<String>();
        List<String> bad = new ArrayList<String>();
        List<Scale> list = service.importList("");
        for (Scale scale : list) {
            String is = service.getOnline("",scale.getName());
            if (service.parseDatabase(scale.getName(), is)) {
                i += 1;
                good.add(scale.getName());
            } else {
                bad.add(scale.getName());
            }
        }
        LOGGER.info("Here is the good list:");
        for (String flag : good) LOGGER.info(flag);
        LOGGER.info("Here is the bad list:");
        for (String flag : bad) LOGGER.info(flag);
        assertEquals(i, list.size());
    }


    /**
     * Test if you can import the participant table.
     *
     * @throws Exception
     */

    @Test
    public void testParticipantDeIdentifiedInfoCorrect() throws Exception {
        repo.flush();
        participant.setTheme("blue");
        participant.setOver18(true);
        participantRepository.save(participant);
        MvcResult result = mockMvc.perform(get("/api/export/Participant")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(result.getResponse().getContentAsString());

        // Clear out all the participants, so we have an empty database.
        participantRepository.deleteAll();
        repo.flush();

        // Assure that the data we have as a json structure contains what we explect it to contain.
        Assert.assertTrue("This should be true:", service.saveParticipant(actualObj.toString()));
        Assert.assertTrue("There should not be an email colomn in your json.", actualObj.get(0).path("email").isMissingNode());
        Assert.assertTrue(actualObj.get(0).path("over18").asBoolean());
        Assert.assertEquals("This should be blue", actualObj.get(0).path("theme").asText(), "blue");
        Assert.assertNotNull("There should be an id specified", actualObj.get(0).path("id").asLong());
        long id = actualObj.get(0).path("id").asLong();
        Participant savedParticipant = participantRepository.findOne(id);
        Assert.assertNotNull("The participant should be saved under the same id as it was before", savedParticipant);

        System.out.println(actualObj);
    }


    /**
     * See if you can import the study table.
     *
     * @throws Exception
     */
    @Test
    public void testStudyInfoCorrect() throws Exception {
        repo.flush();
        s.setConditioning("Testing");
        studyRepository.save(s);
        MvcResult result = mockMvc.perform(get("/api/export/Study")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(result.getResponse().getContentAsString());
        studyRepository.delete(s);
        studyRepository.delete(m);
        Assert.assertTrue("This should be true:", service.parseDatabase("study", actualObj.toString()));
        Assert.assertEquals("This should be testing:", "\"Testing\"", actualObj.get(0).path("conditioning").toString());
        Assert.assertEquals("This should be 1:", 1, actualObj.get(0).path("id").asInt());
    }

    /**
     * See if you can import participant and study tables and then link them back together.
     *
     * @throws Exception
     */

    @Test
    public void testPSInfoCorrect() throws Exception {
        repo.flush();
        participant.setTheme("blue");
        participant.setOver18(true);
        participantRepository.save(participant);
        s.setConditioning("Testing");
        studyRepository.save(s);
        MvcResult result = mockMvc.perform(get("/api/export/Participant")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();

        MvcResult studyResult = mockMvc.perform(get("/api/export/Study")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode actualObj = mapper.readTree(result.getResponse().getContentAsString());

        JsonNode studyObj = mapper.readTree(studyResult.getResponse().getContentAsString());

        MvcResult delResult1 = mockMvc.perform(delete("/api/export/Participant/1")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        System.out.println(delResult1.toString());
        MvcResult delResult2 = mockMvc.perform(delete("/api/export/Participant/2")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        System.out.println(delResult2.toString());

        studyRepository.delete(s);
        studyRepository.delete(m);
        repo.flush();
        Assert.assertTrue("This should be true as well:", service.parseDatabase("study", studyObj.toString()));
        Boolean answer = service.saveParticipant(actualObj.toString());
        Assert.assertTrue("This should be true:", answer);
        Assert.assertTrue("There should not be an email column in your json.", actualObj.get(0).path("email").isMissingNode());
        Assert.assertTrue(actualObj.get(0).path("over18").asBoolean());
        Assert.assertEquals("This should be blue", actualObj.get(0).path("theme").asText(), "blue");
        System.out.println(actualObj);
    }


    /**
     *  Test if you can import the task_log and link it to the study object.
     */

    @Test
    public void testTaskLog() throws Exception {
        createTestEntries();
        MvcResult result = mockMvc.perform(get("/api/export/TaskLog")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode taskObj = mapper.readTree(result.getResponse().getContentAsString());

        taskLogRepository.deleteAll();
        repo.flush();
        Assert.assertTrue("This should be true:",service.parseDatabase("taskLog",taskObj.toString()));
        System.out.println(taskObj.toString());


    }


    /**
     * Test if you can import the questionnaire and logs beside task_log.
     */

    @Test
    public void testGeneral() throws Exception {
        createTestEntries();
        MvcResult result = mockMvc.perform(get("/api/export/TestQuestionnaire")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questObj = mapper.readTree(result.getResponse().getContentAsString());

        repo.delete((long) 1);
        Assert.assertTrue("This should be true:", service.parseDatabase("TestQuestionnaire", questObj.toString()));

        MvcResult resultLog = mockMvc.perform(get("/api/export/EmailLog")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        JsonNode logObj = mapper.readTree(resultLog.getResponse().getContentAsString());

        emailRepo.deleteAll();
        Assert.assertTrue("This should be true:", service.parseDatabase("EmailLog", logObj.toString()));


    }

    /**
     *  Test if you can get a list of files locally.
     * @throws Exception
     */
    @Test
    public void testGetFileList() throws Exception {
        String testScale = "WhatIBelieve";
        File[] list = service.getFileList(testScale);
        for (File file:list) {
            System.out.println(file.getName());
        }
    }


    /**
     *  Test if readJSON is working.
     */
    @Test
    public void testReadJSON() throws Exception {
        String testScale = "WhatIBelieve";
        File[] list = service.getFileList(testScale);
        System.out.println(service.readJSON(list[0]));
    }

    /**
     * Test if you can import the study data locally.
     */

    @Test
    public void testImportStudy() throws Exception {
        assertTrue(service.updateStudyLocal()==0);
    }

    /**
     * This is a test for the missing data log.
     */

    @Test
    public void testMissingLog() throws Exception {
        String tScale ="WhatIBelieve";
        ObjectMapper mapper = new ObjectMapper();
        String is = "[{\"id\": 1, \"date\": \"Sun, 30 Apr 2017 13:49:40 -0500\", \"session\": \"preTest\", \"tag\": null, \"timeOnPage\": null, \"difficultTasks\": 2, \"performEffectively\": 0, \"compared\": 3, \"learn\": 3, \"particularThinking\": 0, \"alwaysChangeThinking\": 4, \"wrongWill\": 2, \"hardlyEver\": 2, \"participant\": \"1\"}]";
        JsonNode obj = mapper.readTree(is);
        assertTrue(service.saveMissingLog(obj.elements().next(),tScale));

    }
    /**
     *  Test if you can import the participant data locally.
     */

    @Test
    public void testImportParticipant() throws Exception {
        service.updateStudyLocal();
        assertTrue(service.updateParticipantLocal() == 0);
    }
    /**
     *  Test if you can import the JsPsych data
     */
    @Test
    public void testJsPsy() throws Exception {
        createTestEntries();
        MvcResult result = mockMvc.perform(get("/api/export/JsPsychTrial")
                .with(SecurityMockMvcRequestPostProcessors.user(admin)))
                .andExpect((status().is2xxSuccessful()))
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questObj = mapper.readTree(result.getResponse().getContentAsString());
        jsPsyRepo.flush();
        jsPsyRepo.deleteAll();
        Assert.assertTrue("This should be true:",service.parseDatabase("JsPsychTrial", questObj.toString()));
        jsPsyRepo.flush();
        System.out.println(questObj.toString());
    }

    @Test
    public void testAutoDownload() throws Exception {
        service.importData();
    }
}


