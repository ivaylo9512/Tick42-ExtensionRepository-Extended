package com.tick42.quicksilver.services;

import com.tick42.quicksilver.config.Scheduler;
import com.tick42.quicksilver.exceptions.GitHubRepositoryException;
import com.tick42.quicksilver.exceptions.UnauthorizedExtensionModificationException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.Spec.GitHubSettingSpec;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Service
public class GitHubServiceImpl implements GitHubService {

    private final GitHubRepository gitHubRepository;
    private final Scheduler scheduler;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private SettingsRepository settingsRepository;
    private UserRepository userRepository;
    private Settings settings;
    private GitHub gitHub;



    @Autowired
    public GitHubServiceImpl(GitHubRepository gitHubRepository, Scheduler scheduler, ThreadPoolTaskScheduler threadPoolTaskScheduler, SettingsRepository settingsRepository, UserRepository userRepository) throws IOException {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.gitHubRepository = gitHubRepository;
        this.scheduler = scheduler;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    @Override
    public void setRemoteDetails(GitHubModel gitHubModel) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Boolean> future = executor.submit(() -> {
            try {

                GHRepository repo = gitHub.getRepository(gitHubModel.getUser() + "/" + gitHubModel.getRepo());

                int pulls = repo.getPullRequests(GHIssueState.OPEN).size();
                int issues = repo.getIssues(GHIssueState.OPEN).size() - pulls;

                Date lastCommit = null;
                List<GHCommit> commits = repo.listCommits().asList();
                if (commits.size() > 0) {
                    lastCommit = commits.get(0).getCommitDate();
                }

                gitHubModel.setPullRequests(pulls);
                gitHubModel.setOpenIssues(issues);
                gitHubModel.setLastCommit(lastCommit);
                gitHubModel.setLastSuccess(new Date());
                return true;
            } catch (GHException e) {
                throw new GitHubRepositoryException("Connected to " + gitHubModel.getLink() + " but couldn't fetch data.");
            } catch (IOException e) {
                throw new GitHubRepositoryException("Couldn't connect to " + gitHubModel.getLink() + ". Check URL.");
            }
        });

        try {
            future.get(50, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            throw new RuntimeException("New Settings are set. Current task canceled.");

        } catch (ExecutionException e){
            e.printStackTrace();
            gitHubModel.setFailMessage(e.getMessage());
            gitHubModel.setLastFail(new Date());

        } catch (TimeoutException e) {
            settings = settingsRepository.findById(settings.getId() + 1)
                    .orElse(settingsRepository.findById(1)
                            .orElseThrow(() -> new RuntimeException("No settings found.")));
        }
    }

    @Override
    public void getRepoDetails(GitHubModel gitHubModel) throws IOException{
        GHRepository repo = gitHub.getRepository(gitHubModel.getUser() + "/" + gitHubModel.getRepo());

        int pulls = repo.getPullRequests(GHIssueState.OPEN).size();
        int issues = repo.getIssues(GHIssueState.OPEN).size() - pulls;

        Date lastCommit = null;
        List<GHCommit> commits = repo.listCommits().asList();
        if (commits.size() > 0) {
            lastCommit = commits.get(0).getCommitDate();
        }

        gitHubModel.setPullRequests(pulls);
        gitHubModel.setOpenIssues(issues);
        gitHubModel.setLastCommit(lastCommit);
        gitHubModel.setLastSuccess(new Date());
    }
    @Override
    public GitHubModel generateGitHub(String link) {
        String[] githubCred = link.replaceAll("https://github.com/", "").split("/");
        String user = githubCred[0];
        String repo = githubCred[1];
        GitHubModel gitHubModel = new GitHubModel(link, user, repo);
        setRemoteDetails(gitHubModel);
        return gitHubModel;
    }

    @Override
    public void updateExtensionDetails() {
        List<GitHubModel> gitHubModels = gitHubRepository.findAll();
        gitHubModels.forEach(gitHub -> {
            setRemoteDetails(gitHub);
            gitHubRepository.save(gitHub);
        });
    }

    @Override
    public void createScheduledTask(int userId, ScheduledTaskRegistrar taskRegistrar, GitHubSettingSpec gitHubSettingSpec) {

        settings = settingsRepository.findById(userId).orElse(new Settings());

        if (gitHubSettingSpec != null) {
            Settings newSettings = new Settings(gitHubSettingSpec);

            if(settings.getId() != null){
                newSettings.setId(settings.getId());
             }
            newSettings.setUser(userRepository.getOne(userId));
            settings = settingsRepository.save(newSettings);
        }

        if (settings.getToken() == null || settings.getUsername() == null) return;

        try {
            gitHub = GitHub.connect(settings.getUsername(), settings.getToken());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to github.");
        }


        if (scheduler.getTask() != null) {
            scheduler.getTask().cancel();
        }

        FixedRateTask updateGitHubData = new FixedRateTask(this::updateExtensionDetails, settings.getRate(), settings.getWait());

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        scheduler.setTask(taskRegistrar.scheduleFixedRateTask(updateGitHubData));
    }

    @Override
    public GitHubSettingSpec getSettings(int userId) {
        Settings userSettings = settingsRepository.findByUser(userRepository.getOne(userId));
        GitHubSettingSpec currentSettings = new GitHubSettingSpec();
        if(userSettings != null){
            currentSettings.setToken(userSettings.getToken());
            currentSettings.setUsername(userSettings.getUsername());
        }
        currentSettings.setRate(settings.getRate());
        currentSettings.setWait(settings.getWait());
        return currentSettings;
    }


    @Override
    public GitHubModel fetchGitHub(GitHubModel gitHub, UserModel loggedUser) {

        if (!loggedUser.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to trigger a github refresh.");
        }
        setRemoteDetails(gitHub);

        return gitHubRepository.save(gitHub);
    }

    @Override
    public void delete(GitHubModel gitHub){
        gitHubRepository.delete(gitHub);
    }
}
