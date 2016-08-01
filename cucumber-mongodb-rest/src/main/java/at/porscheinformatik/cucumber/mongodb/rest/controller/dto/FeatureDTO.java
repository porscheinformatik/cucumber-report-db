package at.porscheinformatik.cucumber.mongodb.rest.controller.dto;

import java.util.List;

public class FeatureDTO
{
    private List<TagDTO> tags;
    private String id;
    private ResultDTO result;
    private String description;
    private String keyword;
    private String name;
    private Integer line;
    private String uri;
    private List<ScenarioDTO> scenarios;

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

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getLine()
    {
        return line;
    }

    public void setLine(Integer line)
    {
        this.line = line;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public List<ScenarioDTO> getScenarios()
    {
        return scenarios;
    }

    public void setScenarios(List<ScenarioDTO> scenarios)
    {
        this.scenarios = scenarios;
    }

    public ScenarioDTO getScenarioById(String id)
    {
        for (ScenarioDTO scenario : scenarios)
        {
            if (scenario.getId().equals(id))
            {
                return scenario;
            }
        }
        return null;
    }

    public ScenarioDTO getScenarioByTag(String tag)
    {
        for (ScenarioDTO scenario : scenarios)
        {
            if (scenario.getTags() != null)
            {
                for (TagDTO currentTag : scenario.getTags())
                {
                    if (currentTag.getName().equals(tag))
                    {
                        return scenario;
                    }
                }
            }
        }
        return null;
    }
}