package com.zaqksdev.el_meyloud_RE.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zaqksdev.el_meyloud_RE.models.Agent;
import com.zaqksdev.el_meyloud_RE.models.Client;
import com.zaqksdev.el_meyloud_RE.models.Offer;
import com.zaqksdev.el_meyloud_RE.models.Visit;
import com.zaqksdev.el_meyloud_RE.repos.AgentRepo;
import com.zaqksdev.el_meyloud_RE.repos.ClientRepo;
import com.zaqksdev.el_meyloud_RE.repos.VisitRepo;
import com.zaqksdev.el_meyloud_RE.vars.VisitVars;

@Service
public class VisitService {
    static VisitRepo visitRepo;
    static ClientRepo clientRepo;
    static AgentRepo agentRepo;
    static OfferService offrSrvc;
    static ContractService cntrctSrvc;
    // vztSrvc clntSrvc agntSrvc
    static VisitService vztSrvc;
    static ClientService clntSrvc;
    static AgentService agntSrvc;
    static PropertyService prprtSrvc;

    @Autowired
    public void setVisitRepo(VisitRepo visitRepo, ClientRepo clientRepo, AgentRepo agentRepo, OfferService offrSrvc,
            ContractService cntrtSrvc, VisitService vztSrvc, ClientService clntSrvc, AgentService agntSrvc,
            PropertyService prprtSrvc

    ) {
        VisitService.visitRepo = visitRepo;
        VisitService.clientRepo = clientRepo;
        VisitService.agentRepo = agentRepo;
        VisitService.offrSrvc = offrSrvc;
        VisitService.cntrctSrvc = cntrtSrvc;

        //
        VisitService.vztSrvc = vztSrvc;
        VisitService.clntSrvc = clntSrvc;
        VisitService.agntSrvc = agntSrvc;
        VisitService.prprtSrvc = prprtSrvc;

    }

    public Visit get(int visitID) {
        return visitRepo.findById(visitID);
    }

    public List<Visit> getOf(String client_email) {
        missingTask();

        return visitRepo.findByClient(clientRepo.findByEmail(client_email));

    }

    public Visit getOf(String client_email, int visitID) {
        missingTask();
        Visit rslt = visitRepo.findById(visitID);

        if (rslt != null && rslt.getClient().getEmail().equals(client_email))
            return rslt;

        return null;

    }

    public List<Visit> getPresentedBy(Agent agent) {
        missingTask();
        return visitRepo.findByAgent(agent);
    }

    public List<Visit> getVisits(int offer_id) {
        missingTask();

        return visitRepo.findByOffer(offrSrvc.get(offer_id));

    }

    public Visit getPresentedBy(String agent_email, int visitID) {
        missingTask();
        Visit rslt = visitRepo.findById(visitID);

        return (rslt != null && rslt.getAgent().equals(agentRepo.findByEmail(agent_email))) ? rslt : null;

    }

    public boolean equalDates(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);

    }

    public List<Visit> filterByDate(List<Visit> inpt, Calendar date) {
        List<Visit> rslt = new ArrayList<Visit>();

        for (int i = 0; i < inpt.size(); i++) {

            // day day month year
            if (equalDates(date, inpt.get(i).getDatetime()))
                rslt.add(inpt.get(i));
        }

        return rslt;
    }

    public Visit getLastOn(Agent agent, Calendar date) {
        List<Visit> todays = filterByDate(getPresentedBy(agent), date);

        HashMap<Integer, Integer> hours = new HashMap<Integer, Integer>();
        HashMap<Integer, Visit> invHours = new HashMap<Integer, Visit>();

        // each indx linkih m3a hour + 1
        int hour;
        for (int i = 0; i < todays.size(); i++) {
            hour = todays.get(i).getDatetime().get(Calendar.HOUR_OF_DAY);
            hours.put(i, hour);
            invHours.put(hour, todays.get(i));
        }

        List<Integer> vals = new ArrayList<Integer>(hours.values());

        Collections.sort(vals);

        // the highest val hya the endtime (last one on the sort)

        if (vals.size() > 0)
            return invHours.get(vals.get(vals.size() - 1));
        else
            return null;

    }

    //

    public List<Visit> getAllRent(String agent_email) {
        List<Visit> inpt = getPresentedBy(agentRepo.findByEmail(agent_email));
        List<Visit> rslt = new ArrayList<Visit>();

        Visit currentVzt;
        Offer currentOffr;
        for (int i = 0; i < inpt.size(); i++) {
            currentVzt = inpt.get(i);

            if (!(currentVzt.isMissed() || currentVzt.isPassed())) {
                currentOffr = inpt.get(i).getOffer();

                if (currentOffr.isChecked() && currentOffr.isRent())
                    if (currentOffr.getProperty().getOwner().getId() != currentVzt.getClient().getId())
                        rslt.add(currentVzt);
            }
        }

        return rslt;
    }

    public List<Visit> getAllBuy(String agent_email) {
        List<Visit> inpt = getPresentedBy(agentRepo.findByEmail(agent_email));
        List<Visit> rslt = new ArrayList<Visit>();

        Visit currentVzt;
        Offer currentOffr;
        for (int i = 0; i < inpt.size(); i++) {
            currentVzt = inpt.get(i);
            currentOffr = inpt.get(i).getOffer();

            if (!(currentVzt.isMissed() || currentVzt.isPassed())) {
                if (currentOffr.isChecked() && !currentOffr.isRent())
                    if (currentOffr.getProperty().getOwner().getId() != currentVzt.getClient().getId())
                        rslt.add(currentVzt);
            }
        }

        return rslt;
    }

    public List<Visit> getAllCheck(String agent_email) {
        List<Visit> inpt = getPresentedBy(agentRepo.findByEmail(agent_email));
        List<Visit> rslt = new ArrayList<Visit>();

        Visit current;
        for (int i = 0; i < inpt.size(); i++) {
            current = inpt.get(i);
            if (!(current.isMissed() || current.isPassed())) {

                if (!(current.getOffer().isChecked()))
                    rslt.add(current);
            }
        }

        return rslt;
    }

    public void save(Visit vst) {
        visitRepo.save(vst);
    }

    public void createVisit(Offer offer, Client client) {
        // sooo lzmlna dabord n3rfou l'agent le plus proche de la propriete
        Agent closestAgnt = prprtSrvc.getClosestAgent(offer.getProperty());
        Calendar nextVisitDate = agntSrvc.getNextVisitDate(closestAgnt, VisitVars.GAP, VisitVars.DURATION);

        Visit visit = new Visit();

        visit.setDatetime(nextVisitDate);
        visit.setOffer(offer);
        visit.setClient(client);
        visit.setAgent(closestAgnt);
        visit.setSelf(offrSrvc.owns(offer, client.getEmail())); //

        vztSrvc.save(visit);
    }

    public void createVisit(int id_offer, String email_client) {
        createVisit(offrSrvc.get(id_offer), clntSrvc.get(email_client));
    }

    //
    public void successVst(int vstID) {
        Visit vst = visitRepo.findById(vstID);
        vst.setPassed(true);

        // create the contract
        cntrctSrvc.createContract(vst);

        // enable the offer or nah
        if (vst.getOffer().isChecked()) {
            // set the offer to non availble
            offrSrvc.deactivateOffer(vst.getOffer());

        } else
            offrSrvc.activateOffer(vst.getOffer());

    }

    public boolean canVisit(int offer_id, String client_email) {
        List<Visit> visits = getOf(client_email);

        // checki la 3ndou une visite not passed
        Visit current;
        for (int i = 0; i < visits.size(); i++) {
            current = visits.get(i);
            if (!current.isPassed())
                return false;
        }

        return true;
    }

    public boolean missedVzt(Visit vzt) {
        Calendar today = Calendar.getInstance();

        return vzt.getDatetime().compareTo(today) < 0;
    }

    public void missingTask() {
        List<Visit> vzts = visitRepo.findAll();

        Visit current;
        for (int i = 0; i < vzts.size(); i++) {
            current = vzts.get(i);

            if (missedVzt(current)) {
                // update
                current.setMissed(true);
                save(current);
                // l ban
            }

        }

    }

}
