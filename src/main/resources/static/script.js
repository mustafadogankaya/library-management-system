document.addEventListener('DOMContentLoaded', () => {
    const bookTableBody = document.getElementById('bookTableBody');
    const addBookForm = document.getElementById('addBookForm');
    const addMessage = document.getElementById('addMessage');
    const listMessage = document.getElementById('listMessage');
    const searchInput = document.getElementById('search');
    const searchButton = document.getElementById('searchButton');
    const listAllButton = document.getElementById('listAllButton');

    const apiUrl = '/api/books';

    // Kitapları getiren ve tabloyu güncelleyen fonksiyon
    const fetchAndDisplayBooks = async (url = apiUrl) => {
        listMessage.textContent = 'Kitaplar yükleniyor...';
        bookTableBody.innerHTML = ''; // Tabloyu temizle
        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const books = await response.json();

            if (books.length === 0) {
                listMessage.textContent = 'Gösterilecek kitap bulunamadı.';
                return;
            }

            books.forEach(book => {
                const row = bookTableBody.insertRow();
                row.innerHTML = `
                    <td>${book.id}</td>
                    <td>${book.title}</td>
                    <td>${book.author}</td>
                    <td>${book.publicationYear}</td>
                    <td>${book.isbn}</td>
                    <td>${book.status}</td>
                    <td>
                        <button class="deleteButton" data-isbn="${book.isbn}">Sil (ISBN)</button>
                        <button class="deleteButtonById" data-id="${book.id}">Sil (ID)</button>
                    </td>
                `;
            });
            listMessage.textContent = ''; // Mesajı temizle
        } catch (error) {
            console.error('Kitaplar getirilirken hata:', error);
            listMessage.textContent = 'Kitaplar yüklenirken bir hata oluştu.';
        }
    };

    // Kitap ekleme formu gönderildiğinde
    addBookForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Formun varsayılan gönderimini engelle
        addMessage.textContent = 'Kitap ekleniyor...';

        const formData = new FormData(addBookForm);
        const bookData = Object.fromEntries(formData.entries());
         // publicationYear'ı sayıya çevir
        bookData.publicationYear = parseInt(bookData.publicationYear, 10);

        try {
            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(bookData),
            });

            if (response.ok) {
                addMessage.textContent = 'Kitap başarıyla eklendi!';
                addBookForm.reset(); // Formu temizle
                fetchAndDisplayBooks(); // Tabloyu güncelle
            } else if (response.status === 409) { // Conflict - Duplicate ISBN
                 const errorText = await response.text();
                addMessage.textContent = `Hata: ${errorText}`;
            }
             else {
                 const errorText = await response.text();
                throw new Error(`Kitap eklenemedi: ${response.status} - ${errorText}`);
            }
        } catch (error) {
            console.error('Kitap eklenirken hata:', error);
            addMessage.textContent = `Bir hata oluştu: ${error.message}`;
        }
    });

    // Silme butonlarına olay dinleyici ekleme (event delegation)
    bookTableBody.addEventListener('click', async (event) => {
        let targetUrl = null;
        let deleteMessage = 'Kitap siliniyor...';

        if (event.target.classList.contains('deleteButton')) {
            const isbn = event.target.dataset.isbn;
            if (confirm(`"${isbn}" ISBN numaralı kitabı silmek istediğinizden emin misiniz?`)) {
                 targetUrl = `${apiUrl}/isbn/${isbn}`;
            }
        } else if (event.target.classList.contains('deleteButtonById')) {
             const id = event.target.dataset.id;
             if (confirm(`"${id}" ID numaralı kitabı silmek istediğinizden emin misiniz?`)) {
                 targetUrl = `${apiUrl}/${id}`;
             }
        }

        if (targetUrl) {
             listMessage.textContent = deleteMessage;
             try {
                const response = await fetch(targetUrl, {
                    method: 'DELETE',
                });

                if (response.ok) { // 204 No Content döner genelde
                    listMessage.textContent = 'Kitap başarıyla silindi.';
                    fetchAndDisplayBooks(); // Tabloyu güncelle
                } else if (response.status === 404) {
                     listMessage.textContent = 'Hata: Silinecek kitap bulunamadı.';
                }
                else {
                    throw new Error(`Kitap silinemedi: ${response.status}`);
                }
            } catch (error) {
                console.error('Kitap silinirken hata:', error);
                listMessage.textContent = `Kitap silinirken bir hata oluştu: ${error.message}`;
            }
        }
    });

     // Arama butonu
    searchButton.addEventListener('click', () => {
        const searchTerm = searchInput.value.trim();
        if (searchTerm) {
            fetchAndDisplayBooks(`${apiUrl}?search=${encodeURIComponent(searchTerm)}`);
        } else {
             listMessage.textContent = 'Lütfen bir arama terimi girin.';
        }
    });

     // Tümünü Listele butonu
    listAllButton.addEventListener('click', () => {
        searchInput.value = ''; // Arama kutusunu temizle
        fetchAndDisplayBooks(); // Tüm kitapları getir
    });


    // Sayfa ilk yüklendiğinde kitapları getir
    fetchAndDisplayBooks();
});
