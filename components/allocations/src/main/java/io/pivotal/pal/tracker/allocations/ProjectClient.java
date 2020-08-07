package io.pivotal.pal.tracker.allocations;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.RestOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;
    private final Map<Long,ProjectInfo> projectInfoCache = new ConcurrentHashMap<>();
    private final Logger logger=LoggerFactory.getLogger(getClass());

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations= restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo projectInfo= restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);
        projectInfoCache.put(projectId,projectInfo);
        return projectInfo;
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause){
        logger.info("Getting project with id {} from cache", projectId);
        //logger.info("Print throwable cause", cause);
        return projectInfoCache.get(projectId);
    }
}
