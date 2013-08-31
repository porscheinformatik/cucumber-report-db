package at.porscheinformatik.common.utils.silkplugin.dto;

import java.math.BigInteger;

public class StepResultDTO
{
    private BigInteger duration;
    private String status;
    private String error_message;

    public BigInteger getDuration()
    {
        return duration;
    }

    public void setDuration(BigInteger duration)
    {
        this.duration = duration;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getError_message()
    {
        return error_message;
    }

    public void setError_message(String error_message)
    {
        this.error_message = error_message;
    }
}
