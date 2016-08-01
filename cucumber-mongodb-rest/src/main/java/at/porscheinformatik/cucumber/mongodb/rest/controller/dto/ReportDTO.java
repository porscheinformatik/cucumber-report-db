package at.porscheinformatik.cucumber.mongodb.rest.controller.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Stefan Mayer (yms)
 */
public class ReportDTO
{
    private List<FeatureDTO> features;
    private DateDTO date;

    public List<FeatureDTO> getFeatures()
    {
        return features;
    }

    public void setFeatures(List<FeatureDTO> features)
    {
        this.features = features;
    }

    public Date getDate()
    {
        return getCalendar().getTime();
    }

    public Calendar getCalendar()
    {
        Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(date.get$date());
        cal.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        return cal;
    }

    public DateDTO getDateDTO()
    {
        return date;
    }

    public void setDateDTO(DateDTO date)
    {
        this.date = date;
    }
}
