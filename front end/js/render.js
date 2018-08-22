var render = (() => {
    var searchResults = (results, title) => {
        var extensions = {
            results,
            title
        }
        show.searchResults(extensions)
    }
    var uploadsResult = (results, count) => {
        var extensions = {
            results,
            count
        }
        show.uploadsResult(extensions)
    }

    var downloadsResult = (results, count) => {
        var extensions = {
            results,
            count
        }
        show.mostDownloadsResult(extensions)
    }

    return {
        searchResults,
        uploadsResult,
        downloadsResult
    }
})();