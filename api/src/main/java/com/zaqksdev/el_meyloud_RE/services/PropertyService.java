package com.zaqksdev.el_meyloud_RE.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.zaqksdev.el_meyloud_RE.models.Property;
import com.zaqksdev.el_meyloud_RE.repos.ClientRepo;
import com.zaqksdev.el_meyloud_RE.repos.PropertyRepo;

@Service
public class PropertyService {
    static PropertyRepo propertyRepo;
    static ClientRepo clientRepo;

    @Autowired
    public void setPropertyRepo(PropertyRepo propertyRepo, ClientRepo clientRepo) {
        PropertyService.propertyRepo = propertyRepo;
        PropertyService.clientRepo = clientRepo;

    }

    public Boolean exists(Property property, BindingResult result) {
        // check the addr
        if (propertyRepo.findByAddr(property.getAddr()) != null) {
            result.rejectValue("addr", null, "already existing");
            return true;
        }
        // check the coords
        float x = property.getX();
        float y = property.getY();
        int i;

        List<Property> xlst = propertyRepo.findByX(x);
        List<Property> ylst = propertyRepo.findByX(y);

        for (i = 0; i < xlst.size(); i++) {
            if (xlst.get(i).getY() == y) {
                result.rejectValue("x", null, "already existing");
                result.rejectValue("y", null, "already existing");
                return true;
            }
        }

        for (i = 0; i < ylst.size(); i++) {
            if (ylst.get(i).getX() == x) {
                result.rejectValue("x", null, "already existing");
                result.rejectValue("y", null, "already existing");
                return true;
            }
        }

        return false;
    }

    public List<Property> getOf(String email) {

        return propertyRepo.findByOwner(clientRepo.findByEmail(email));
    }

    public Property getOf(String email, int prp_id) {
        Property rslt = propertyRepo.findById(prp_id);
        if (rslt == null)
            return null;

        if (!owns(email, prp_id))
            return null;

        return rslt;
    }

    public boolean owns(String email, int prp_id) {
        Property rslt = propertyRepo.findById(prp_id);
        return rslt.getOwner().getEmail().equals(email);
    }

    public void save(Property prp) {
        propertyRepo.save(prp);
    }

}
