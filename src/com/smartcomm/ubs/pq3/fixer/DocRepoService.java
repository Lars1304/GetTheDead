package com.smartcomm.ubs.pq3.fixer;


import com.assentis.adb.common.util.ADBUtility;
import com.assentis.docrepo.service.common.DocRepoConstants;
import com.assentis.docrepo.service.common.ServiceUtil;
import com.assentis.docrepo.service.iface.PublicMutator;
import com.assentis.docrepo.service.iface.PublicSearch;
import com.assentis.docrepo.service.iface.bean.*;

import java.net.URL;
import java.rmi.RemoteException;

public class DocRepoService {


    private static final String FILE_CHANGED = "Automatically changed by cleaning tool";
    private static final String[] TEXT_EXP_COMP = new String[]{DocRepoConstants.FILETYPE_TEXTEXP,
            DocRepoConstants.FILETYPE_TEXTCOMP};

    private PublicMutator publicMutator;
    private PublicSearch publicSearch;

    public DocRepoService(String docRepoUrl, String username, String password) throws Exception {
        init(docRepoUrl, username, password);
    }

    private void init(String docRepoUrl, String username, String password) throws Exception {
        publicMutator = ServiceUtil.createPublicMutatorServiceStub(
                new URL(docRepoUrl), username, password);

        publicSearch = ServiceUtil.createPublicSearchServiceStub(
                new URL(docRepoUrl), username, password);
    }

    public SearchResult[] getRepositories() throws Exception {
        final SearchOptions searchOptions = new SearchOptions();
        searchOptions.setIncludeRepositories(true);
        final FileFinder ff = new FileFinder();
        searchOptions.setExcludeFiles(true);
        final SearchResult[] results = publicSearch.search(new FileFinder[]{ff}, searchOptions);
        return results;
    }

    public SearchResult[] getFolders(final long repoDbKey) throws Exception {
        final SearchOptions searchOptions = new SearchOptions();
        searchOptions.setIncludeFolders(true);
        final FileFinder ff = new FileFinder();
        searchOptions.setExcludeFiles(true);
        final Repository context = new Repository();
        context.setDBKey(repoDbKey);
        searchOptions.setContext(context);
        final SearchResult[] results = publicSearch.search(new FileFinder[]{ff}, searchOptions);
        return results;
    }

    public void updateContent(long dbKey, byte[] content) throws Exception {
        byte[] zippedContent = ADBUtility.zipElementContent(content);
        publicMutator.unlockFile(dbKey);
        publicMutator.lockFile(dbKey);
        publicMutator.updateFileContent(dbKey, zippedContent, false, true);

    }

    /*
        public SearchResult getRepository() throws Exception, ApplicationException, SystemException {
            final SearchOptions searchOptions = new SearchOptions();
            searchOptions.setIncludeRepositories(true);
            final FileFinder ff = new FileFinder();
            searchOptions.setExcludeFiles(true);
            ff.setDBKey(CleanerCLI.repositoryDBKey);
            final SearchResult[] results = publicSearch.search(new FileFinder[]{ff}, searchOptions);
            if (results == null || results.length == 0) {
                return null;
            }
            return results[0];
        }
    */
    public Long getDbKeyByElementId(final String elementId) throws Exception {
        final FileFinder ff = new FileFinder();
        ff.setUuid(elementId);
        final long[] dbKeys = publicSearch.searchReturningDBKeys(new FileFinder[]{ff}, new SearchOptions());
        if (dbKeys == null || dbKeys.length != 1) {
            return null;
        }
        return dbKeys[0];
    }

    public SearchResult[] findElementsByType(final String type) throws Exception {
        final FileFinder ff = new FileFinder();
        ff.setType(type);
        final SearchOptions searchOptions = new SearchOptions();
        final SearchResult[] results = publicSearch.search(new FileFinder[]{ff}, searchOptions);
        return results;
    }

    /*
    public long[] findElementDBKeysByType(final String type) throws Exception {
        LOGGER.debug("Scanning for elements of type " + type);
        final FileFinder ff = new FileFinder();
        ff.setType(type);
        final SearchOptions searchOptions = new SearchOptions();
        final long[] results = publicSearch.searchReturningDBKeys(new FileFinder[]{ff}, searchOptions);
        LOGGER.debug("Found " + results.length + " elements of type " + type);
        return results;
    }

    public long[] getDependencies(final SearchResult result) {
        final ProtectedItem item = result.getItem();
        final SearchOptions searchOptions = new SearchOptions();
        searchOptions.setReferencingFileDbKey(item.getDBKey());
        try {
            return publicSearch.searchReturningDBKeys(new FileFinder[]{new FileFinder()}, searchOptions);
        } catch (final Exception e) {
            LOGGER.error("Failed to determine dependencies for " + item.getName() + "[" + item.getDBKey() + "]", e);
        }
        return null;
    }

    public long[] getDependenciesForTextComp(final SearchResult result) {
        final FileFinder fileFinder = new FileFinder();
        fileFinder.setType_IN(TEXT_EXP_COMP);
        final ProtectedItem item = result.getItem();
        final SearchOptions searchOptions = new SearchOptions();
        searchOptions.setReferencingFileDbKey(item.getDBKey());
        try {
            return publicSearch.searchReturningDBKeys(new FileFinder[]{fileFinder}, searchOptions);
        } catch (final Exception e) {
            LOGGER.error("Failed to determine dependencies for " + item.getName() + "[" + item.getDBKey() + "]", e);
        }
        return null;
    }
*/
    public byte[] getContents(final long dbKey) throws Exception {
        final File contents = publicSearch.getFile(dbKey, true);
        if (contents == null || contents.getContentZipped() == null || contents.getContentZipped().length == 0) {
            throw new IllegalStateException("Failed to determine element with dbKey=" + dbKey);
        }
        return ADBUtility.unzipElementContent(contents.getContentZipped());
    }

    public long[] getReferencedBy(long dbKey) throws RemoteException {
        final SearchOptions searchOptions = new SearchOptions();
        searchOptions.setReferencingFileDbKey(dbKey);
        return publicSearch.searchReturningDBKeys(new FileFinder[]{new FileFinder()}, searchOptions);
    }

    /*
    public List<File> changeContents(final Map<SearchResult, byte[]> variableSets) throws Exception {
        final List<File> updatedFiles = new ArrayList<>();
        final List<Future<Object>> futures = new ArrayList<>();
        for (final SearchResult variableSet: variableSets.keySet()) {
            final long dbKey = variableSet.getItem().getDBKey();
            final byte[] contents = variableSets.get(variableSet);
        }
        while (futures.size() > 0) {
            try {
                Future f = executorContainer.getCompletionService().take();
                futures.remove(f);
                final File updatedFile = (File) f.get();
                if (updatedFile != null) {
                    LOGGER.debug("Updated element with DBKey " + updatedFile.getDBKey());
                    updatedFiles.add(updatedFile);
                }
            } catch (Throwable t) {
                LOGGER.error("An error occured upon updating elements", t);
            }
        }
        executorContainer.initContainer();
        return updatedFiles;
    }

    public List<SearchResult> getElements(final List<String> elementIds) {
        final List<SearchResult> searchResults = new ArrayList<>();
        final List<Future<Object>> futures = new ArrayList<>();
        for (final String elementId: elementIds) {
            futures.add(executorContainer.getCompletionService().submit(() -> {
                final SearchResult result = findElementByElemId(elementId);
                searchResults.add(result);
                return null;
            }));
        }
        while (futures.size() > 0) {
            try {
                Future f = executorContainer.getCompletionService().take();
                futures.remove(f);
                f.get();
            } catch (Throwable t) {
                LOGGER.error("An error occured upon determining templates", t);
            }
        }
        executorContainer.initContainer();
        return searchResults;
    }

    public List<File> deleteElements(final String type, final Collection<Long> dbKeys) {
        List<File> deletedElements = new ArrayList<>();
        final List<Future<Object>> futures = new ArrayList<>();
        if (type.equals(DocRepoConstants.FILETYPE_DWTEMPLATE) || type.equals(DocRepoConstants.FILETYPE_DPMODLIB) ||
                type.equals(DocRepoConstants.FILETYPE_WORKSPACE)  || type.equals(DocRepoConstants.FILETYPE_TEXTCOMPVARSET)) {
            try {
                referenceRemover.deleteReferences(dbKeys);
            } catch (Exception e) {
                LOGGER.error("Failed to delete depenendency references", e);
                return deletedElements;
            }
        }
        for (final Long dbKey : dbKeys) {
            futures.add(executorContainer.getCompletionService().submit(() -> deleteElement(dbKey)));
        }
        while (futures.size() > 0) {
            try {
                Future f = executorContainer.getCompletionService().take();
                futures.remove(f);
                final File deletedFile = (File) f.get();
                if (deletedFile != null) {
                    LOGGER.debug("Deleted element with DBKey " + deletedFile.getDBKey());
                    deletedElements.add(deletedFile);
                }
            } catch (Throwable t) {
                LOGGER.error("An error occured upon deleting elements", t);
            }
        }
        executorContainer.initContainer();
        return deletedElements;
    }

    public boolean deleteFolder(final long dbKey) throws Exception {
        final FileFinder ff = new FileFinder();
        final SearchOptions searchOptions = new SearchOptions();
        final Folder context = new Folder();
        context.setDBKey(dbKey);
        searchOptions.setContext(context);
        final long[] results = publicSearch.searchReturningDBKeys(new FileFinder[]{ff}, searchOptions);
        if (results == null || results.length == 0) {
            publicMutator.deleteFolder(dbKey, true, true);
            return true;
        }
        return false;
    }

    private SearchResult findElementByElemId(final String elementId) throws Exception {
        final FileFinder ff = new FileFinder();
        ff.setUuid(elementId);

        final SearchResult[] results = publicSearch.search(new FileFinder[]{ff}, new SearchOptions());
        if (results == null || results.length == 0) {
            throw new IllegalStateException("Found no result for elementId " + elementId);
        }
        return results[0];
    }

    private File deleteElement(final long dbKey) {
        try {
            publicMutator.unlockFile(dbKey);
            publicMutator.lockFile(dbKey);
            return publicMutator.deleteFile(dbKey, true);
        } catch (final Exception ex) {
            LOGGER.error("Failed to delete element with key " + dbKey, ex);
        }
        return null;
    }
*/
    public SearchResult findElementByDbKey(final Long dbKey) throws Exception {
        final FileFinder ff = new FileFinder();
        ff.setDBKey(dbKey);

        final SearchResult[] results = publicSearch.search(new FileFinder[]{ff}, new SearchOptions());
        if (results == null || results.length == 0) {
            throw new IllegalStateException("Found no result for elementId " + dbKey);
        }
        return results[0];
    }

}
