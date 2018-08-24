package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.GitHub;
import com.tick42.quicksilver.models.User;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ExtensionsServiceImpl implements ExtensionService {

    private final ExtensionRepository extensionRepository;
    private final TagService tagService;
    private final GitHubService gitHubService;

    @Autowired
    public ExtensionsServiceImpl(ExtensionRepository extensionRepository, TagService tagService, GitHubService gitHubService) {
        this.extensionRepository = extensionRepository;
        this.tagService = tagService;
        this.gitHubService = gitHubService;
    }

    @Override
    public Extension create(ExtensionDTO extensionDTO) {

        Extension extension = new Extension(extensionDTO);

        User mockUser = new User();
        mockUser.setId(3);

        extension.setIsPending(true);
        extension.setOwner(mockUser);
        extension.setUploadDate(new Date());
        extension.setGithub(gitHubService.generateGitHub(extensionDTO.getGithub()));
        extension.setTags(tagService.generateTags(extensionDTO.getTags()));

        return extensionRepository.create(extension);
    }

    @Override
    public Extension findById(int id) {
        return extensionRepository.findById(id);
    }

    @Override
    public void delete(int id) {
        extensionRepository.delete(id);
    }

    @Override
    public List<Extension> findByName(String searchQuery) {
        return extensionRepository.findByName(searchQuery);
    }


    @Override
    public List<Extension> findAll() {
        return extensionRepository.findAll();
    }

    @Override
    public List<Extension> findTopMostDownloaded(int count) {
        return extensionRepository.findTopMostDownloaded(count);
    }

    @Override
    public List<Extension> findMostRecentUploads(int count) {
        return extensionRepository.findMostRecentUploads(count);
    }

    @Override
    public List<Extension> findFeatured(int count) {
        return extensionRepository.findFeatured(count);
    }

    @Override
    public List<Extension> sortByUploadDate() {
        return extensionRepository.sortByUploadDate();
    }

    @Override
    public List<Extension> sortByMostDownloads() {
        return extensionRepository.sortByMostDownloads();
    }

    @Override
    public List<Extension> sortByCommitDate() {
        return extensionRepository.sortByCommitDate();
    }

    @Override
    public List<Extension> findByTag(String tagName) {
        return tagService.findByName(tagName).getExtensions();
    }

    @Override
    public void approveExtension(int id) {
        Extension extension = extensionRepository.findById(id);
        extension.setIsPending(false);
        extensionRepository.update(extension);
    }

    @Override
    public void changeFeaturedState(int id) {
        Extension extension = extensionRepository.findById(id);
        if (extension.getIsFeatured()) {
            extension.setIsFeatured(false);
            extensionRepository.update(extension);
        } else {
            extension.setIsFeatured(true);
            extensionRepository.update(extension);
        }
    }
}
