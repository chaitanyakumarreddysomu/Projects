$(document).ready(function() {
    var $chat = $('.chat');
    var $messages = $('.messages-content');

    $('#chatbot-button').click(function() {
        $chat.toggleClass('active');
    });

    $('.close-btn').click(function() {
        $chat.removeClass('active');
    });

    $('.message-submit').click(function() {
        insertMessage();
    });

    $(window).on('keydown', function(e) {
        // Check for Alt + Shift + C
        if (e.altKey && e.shiftKey && e.which === 67) {
            $chat.toggleClass('active'); // Toggle chat visibility
            return false; // Prevent default behavior
        }
        // Check for Enter key
        if (e.which === 13) {
            insertMessage();
            return false; // Prevent default behavior
        }
    });

    function updateScrollbar() {
        $messages.scrollTop($messages[0].scrollHeight);
    }

    function setDate() {
        var now = new Date();
        var timestamp = now.getHours() + ':' + (now.getMinutes() < 10 ? '0' : '') + now.getMinutes();
        $('<div class="timestamp">' + timestamp + '</div>').appendTo($('.message:last'));
    }

    function insertMessage() {
        var msg = $('.message-input').val().trim();
        if (msg === '') return false;

        $('<div class="message message-personal">' + msg + '</div>').appendTo($messages).addClass('new');
        setDate();
        $('.message-input').val(null);
        updateScrollbar();

        // Call backend to get product suggestions
        fetchProductSuggestions(msg);
    }

    // Frontend JavaScript remains the same
    function fetchProductSuggestions(query) {
        $('.message.loading').remove(); // Remove any previous loading message
        $('<div class="message loading"><span>Loading...</span></div>').appendTo($messages);
        updateScrollbar();

        $.ajax({
            url: '/api/products/suggest',
            method: 'GET',
            data: { query: query },
            success: function(data) {
                $('.message.loading').remove();
                if (data && data.length > 0) {
                    var message = '<div class="message message-received new">';
                    data.forEach(function(product) {
                        message += 
									'<div class="product-cart-wrap mb-30">' +
						
                                   '<div class="product-img-action-wrap">' +
                                   '<div class="product-img product-img-zoom">' +
                                   '<a href="/product/' + product.id + '">' +
                                   '<img alt="" src="/img/product_img/' + product.image + '" width="150px" height="150px">' +
                                   '</a>' +
                                   '</div>' +
                                   '<div class="product-badges product-badges-position product-badges-mrg">' +
                                   '<span class="hot">' + product.discount + '% off</span>' +
                                   '</div>' +
                                   '</div>' +
                                   '<div class="product-content-wrap">' +
                                   '<div class="product-category">' +
                                   // Optionally display product category
                                   '</div>' +
                                   '<h2><a style="display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis;" href="/product/' + product.id + '">' + product.title + '</a></h2>' +
                                   '<div class="product-card-bottom">' +
                                   '<div class="product-price">' +
                                   '<span>$' + product.discountPrice.toFixed(2) + '</span>' +
                                   '<span class="old-price">$' + product.price.toFixed(2) + '</span>' +
                                   '</div>' +
                                   '</div>' +
                                   '</div>' +
                                   '</div>';
                    });
                    message += '</div>';
                    $(message).appendTo($messages);
                } else {
                    $('<div class="message message-received new">No products found matching "' + query + '".</div>').appendTo($messages);
                }
                setDate();
                updateScrollbar();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("AJAX Error: ", textStatus, errorThrown);
                $('.message.loading').remove();
                $('<div class="message message-received new">Sorry, there was an error fetching the products. Please check the console for more details.</div>').appendTo($messages);
                setDate();
                updateScrollbar();
            }
        });
    }

    // Initialize chat
    $('<div class="message message-received new">Hello! How can I assist you today?</div>').appendTo($messages);
    setDate();
    updateScrollbar();
});
