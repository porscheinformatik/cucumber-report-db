package at.porscheinformatik.common.utils.silkplugin.dto;

import java.util.List;

public class ScenarioDTO
{
    private String id;
    private List<TagDTO> tags;
    private ResultDTO result;
    private String description;
    private String name;
    private String keyword;
    private Integer line;
    private List<StepDTO> steps;
    private String type;

    public List<TagDTO> getTags()
    {
        return tags;
    }

    public void setTags(List<TagDTO> tags)
    {
        this.tags = tags;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public ResultDTO getResult()
    {
        return result;
    }

    public void setResult(ResultDTO result)
    {
        this.result = result;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public Integer getLine()
    {
        return line;
    }

    public void setLine(Integer line)
    {
        this.line = line;
    }

    public List<StepDTO> getSteps()
    {
        return steps;
    }

    public void setSteps(List<StepDTO> steps)
    {
        this.steps = steps;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
