function deleteAllCookies() {
    const cookies = document.cookie.split(";");

    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i];
        const eqPos = cookie.indexOf("=");
        const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
    }

    window.location.href = window.location.href.split('?')[0] + '?nocache=' + new Date().getTime();
    window.location.reload(true);
}

function clearAndRedirect(link) {
    deleteAllCookies();
    document.location = link;
}