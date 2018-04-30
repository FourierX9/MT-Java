package org.mindtrails.domain;

import com.sun.tools.javac.comp.Flow;
import lombok.Data;
import org.mindtrails.domain.jsPsych.JsPsychTrial;
import org.mindtrails.domain.tracking.TaskLog;
import org.mindtrails.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class FlowCount {

    private ArrayList<countMap> pairs = new ArrayList<countMap>();
    private Long accessAttempt;
    private Long passAttempt;
    //These will change according to condition or gender.
    private Long totalAccount;
    private Long ittCount;
    private List<Session> sessionList;
    private Long duplicated = Long.valueOf(0);


    public FlowCount(Study study, StudyRepository studyRepository, VisitRepository visitRepository, ParticipantRepository participantRepository, JsPsychRepository jsPsychRepository,TaskLogRepository taskLogRepository){
        List<Participant> realAccount = participantRepository.findParticipantsByTestAccountIsFalseAndAdminIsFalse();
        List<Study> realStudies = realAccount.stream()
                .map(Participant::getStudy).collect(Collectors.toList());
        List<Long> realID = realAccount.stream().map(Participant::getId).collect(Collectors.toList());
        //List<Study> realStudies = studyRepository.findDistinctByParticipantIn(realAccount);
        this.accessAttempt = Long.valueOf(visitRepository.findAll().size());
        this.passAttempt = this.accessAttempt - visitRepository.findAllByNameEndsWith("_not_pass").size();
        this.totalAccount = Long.valueOf(realAccount.size());
        this.ittCount = jsPsychRepository.findDistinctByParticipantIn(realID).stream().map(JsPsychTrial::getParticipant).collect(Collectors.toList()).stream().distinct().count();

        this.pairs.add(new countMap("Assessed for eligibility",this.accessAttempt,duplicated));
        this.pairs.add(new countMap("Eligible", this.passAttempt,duplicated));
        this.pairs.add(new countMap("Created an account", this.totalAccount,duplicated));
        this.pairs.add(new countMap("Intent to treat",this.ittCount,duplicated));
        this.sessionList = study.getSessions();
        for (Session session: this.sessionList) {
            if (!session.getName().toLowerCase().equals("complete")) {
                if (session.getName().toLowerCase().equals("pretest")||session.getName().toLowerCase().equals("postfollowup")) {
                    this.pairs.add(new countMap("Completed " + session.getName(), Long.valueOf(taskLogRepository.findDistinctByStudyInAndSessionNameAndTaskName(realStudies,session.getName(), "SESSION_COMPLETE").size()),duplicated));
                } else {
                    this.pairs.add(new countMap("Completed " + session.getName(), Long.valueOf(taskLogRepository.findDistinctByStudyInAndSessionNameAndTaskName(realStudies,session.getName(), "JsPsychTrial").size()),duplicated));
                }
            }
        }
    }

}
