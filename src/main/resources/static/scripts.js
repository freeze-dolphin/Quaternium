function deleteAllCookies() {
    fetch('/clear-session', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    }).then(r => {
        if (!r.ok) {
            const cookies = document.cookie.split(";");

            for (let i = 0; i < cookies.length; i++) {
                const cookie = cookies[i];
                const eqPos = cookie.indexOf("=");
                const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
                document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
            }
        }
    })
}

function clearAndRedirect(link) {
    deleteAllCookies();
    document.location = link;
    location.reload()
}

function redirect(link) {
    document.location = link;
    location.reload()
}