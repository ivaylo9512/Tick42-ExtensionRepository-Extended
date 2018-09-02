remote = (() => {

    const base = "http://localhost:8080";

    let isAuth = () => {
        return localStorage.getItem('Authorization') !== null;
    }

    let getTag = (tagName) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/tag/" + tagName,
        })
    }

    let loadByUploadDate = (name, page, perPage) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/filter" + "?name=" + name + "&orderBy=date" + "&page=" + page + "&perPage=" + perPage
        })
    }

    let loadByName = (name, page, perPage) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/filter" + "?name=" + name + "&orderBy=name" + "&page=" + page + "&perPage=" + perPage
        })
    }

    let loadByTimesDownloaded = (name, page, perPage) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/filter" + "?name=" + name + "&orderBy=downloads" + "&page=" + page + "&perPage=" + perPage
        })
    }

    let loadByLatestCommit = (name, page, perPage) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/filter" + "?name=" + name + "&orderBy=commits" + "&page=" + page + "&perPage=" + perPage
        })
    }

    let loadFeatured = () => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/featured"
        })
    }

    let submitExtension = (extension) => {
        return $.ajax({
            type: 'POST',
            url: base + "/api/extensions",
            data: JSON.stringify(extension),
            contentType: 'application/json',
            headers: {
                "Authorization": localStorage.getItem("Authorization")
            },
        })
    }

    let getUserProfile = (id) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/user/" + id,
            headers: {
                "Authorization": localStorage.getItem("Authorization")
            },
        })
    }

    let login = (user) => {
        return $.ajax({
            type: 'POST',
            url: base + "/api/user/login",
            data: JSON.stringify(user),
            contentType: 'application/json'
        })
    }

    let register = (user) => {
        return $.ajax({
            type: 'POST',
            url: base + "/api/user/register",
            data: JSON.stringify(user),
            contentType: 'application/json'
        })
    }

    let setPublishedState = (id, state) => {
        return $.ajax({
            type: 'PATCH',
            url: base + '/api/extensions/' + id + '/status/' + state
        })
    }

    let setFeaturedState = (id, state) => {
        return $.ajax({
            type: 'PATCH',
            url: base + '/api/extensions/' + id + '/featured/' + state
        })
    }

    let deleteExtension = (id) => {
        return $.ajax({
            type: 'DELETE',
            url: base + '/api/extensions/' + id,
            headers: {
                'Authorization': localStorage.getItem('Authorization')
            },
        })

    }

    let setUserState = (id, state) => {
        return $.ajax({
            type: 'PATCH',
            url: base + '/api/user/setState/' + id + '/' + state,
            headers: {
                'Authorization': localStorage.getItem('Authorization')
            }
        })
    }

    let getExtension = (id) => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/" + id
        })
    }

    let getAllUsers = () => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/user/get"
        })
    }
    let getActive = () => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/user/get" + "?state=active"
        })
    }

    let getBlocked = () => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/user/get" + "?state=blocked"
        })
    }

    let loadPending = () => {
        return $.ajax({
            type: 'GET',
            url: base + "/api/extensions/unpublished"
        })
    }


    return {
        isAuth,
        getTag,
        loadByUploadDate,
        loadByName,
        loadByTimesDownloaded,
        loadByLatestCommit,
        submitExtension,
        getUserProfile,
        login,
        getAllUsers,
        getActive,
        getBlocked,
        getExtension,
        setPublishedState,
        setFeaturedState,
        deleteExtension,
        loadFeatured,
        loadPending,
        setUserState,
        register
    }
})()
