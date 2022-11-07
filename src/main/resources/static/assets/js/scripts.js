'use strict';
(function ($) {

    var px = ''; //'rt--'

    /**
     * Функция для вывода набора jQuery по селектору, к селектору добавляются
     * префиксы
     *
     * @param {string} selector Принимает селектор для формирования набора
     * @return {jQuery} Возвращает новый jQuery набор по выбранным селекторам
     */
    function $x(selector) {
        return $(x(selector));
    }

    /**
     * Функция для автоматического добавления префиксов к селекторы
     *
     * @param {string} selector Принимает селектор для формирования набора
     * @return {string} Возвращает новый jQuery набор по выбранным селекторам
     */
    function x(selector) {
        var arraySelectors = selector.split('.'),
            firstNotClass = !!arraySelectors[0];

        selector = '';

        for (var i = 0; i < arraySelectors.length; i++) {
            if (!i) {
                if (firstNotClass) selector += arraySelectors[i];
                continue;
            }
            selector += '.' + px + arraySelectors[i];
        }

        return selector;
    }

    $(function () {var button = function(){
    return {
        init: function(){
            $('.Site').on('click', 'a.btn[disabled="disabled"]', function(e){
                e.preventDefault();
            });
        }
    };
};
button().init();

var form = function(){
    var $selectList = $('.selectList');
    var $input = $('.form-input, .form-textarea');
    var $form = $('.form');
    var $select = $('.form-select');
    return {
        init: function(){
            $selectList.each(function(){
                var $this = $(this),
                    $radio= $this.find('input[type="radio"]');
                function changeTitle($block, $element) {
                    $block.find('.selectList-title')
                        .text( $element.closest('.selectList-item')
                            .find('.selectList-text').text())
                }
                changeTitle($this, $radio.filter('[checked="checked"]'));
                $radio.on('change', function(){
                    changeTitle($this, $(this));
                });
                
            });
            $(document).on('click', function(e){
                var $this = $(e.target);
                if (!$this.hasClass('selectList-header') ) {
                    $this = $(e.target).closest('.selectList-header');
                }
                if ( $this.length ){
                    e.preventDefault();
                    $this.closest('.selectList').toggleClass('selectList_OPEN');
                } else {
                    $('.selectList').removeClass('selectList_OPEN');
                }
            });
            
            // Валидация полей
            $input.on('blur', function(){
                var $this = $(this),
                    validate = $this.data('validate'),
                    message = '',
                    error = false;
                if (validate){
                    validate = validate.split(' ');
                    validate.forEach(function(v){
                        switch (v){
                            case 'require':
                                if (!$this.val()
                                    && !$this.prop('disabled')
                                    ) {
                                        message += 'Это поле обязательно для заполнения. ';
                                        error = true;
                                }
                                break;
                            case 'mail':
                                if ($this.val()!==''
                                    && !$this.val().match(/\w+@\w+\.\w+/)
                                    && !$this.prop('disabled')
                                    ) {
                                        message += 'Нужно ввести адрес почты в формате xxx@xxx.xx';
                                        error = true;
                                }
                                break;
                            case 'key':
                                if ($this.val()!==''
                                    && !$this.val().replace(' ', '').match(/\d{6}/)
                                    && !$this.prop('disabled')
                                    ){
                                        message += 'Код должен состоять из 6 цифр';
                                        error = true;
                                }
            
                        }
                    });
                    
                    if (error) {
                        if ($this.hasClass('form-input')){
                            $this.addClass('form-input_error');
                        }
                        if ($this.hasClass('form-textarea')){
                            $this.addClass('form-textarea_error');
                        }
                        if (!$this.next('.form-error').length){
                            $this.after('<div class="form-error">'+message+'</div>');
                        } else {
                            $this.next('.form-error').text(message);
            
                        }
                        $this.data('errorinput', true);
                    } else {
                        $this.next('.form-error').remove();
                        $this.removeClass('form-input_error');
                        $this.removeClass('form-textarea_error');
                        $this.data('errorinput', false);
                    }
                    message = '';
                }
            });
            $form.on('submit', function(e){
                var $this = $(this),
                    $validate = $this.find('[data-validate]');
                
                $validate.each(function(){
                    var $this = $(this);
                    $this.trigger('blur');
                    if ($this.data('errorinput')){
                        e.preventDefault();
                    }
                });
            });
            $select.wrap('<div class="form-selectWrap"></div>');
            $('[data-mask]').each(function(){
                var $this = $(this);
                $this.mask($this.data('mask'), {placeholder:'x'});
            });
        }
    };
};
form().init();

var menu = function(){
    var $menuMain = $('.menu_main');
    $menuMain.css('position', 'absolute');
    var menuHeight = $('.menu_main').outerHeight();
    $menuMain.css('position', 'static');
    var $body = $('body');
    function refresh(){
        if (window.innerWidth<991) {
            // $('.menuModal').each(function(){
            //     var $this = $(this);
            //     setTimeout(function(){
            //         if ($this.attr('height') > 0) {
            //             $this.css('height', 0);
            //         }
            //     }, 100);
            // });
            $('.menuModal').css('height', 0);
            $menuMain.css('position', 'absolute');
            menuHeight = $('.menu_main').outerHeight();
            $menuMain.css('position', 'static');
        } else {
            menuHeight = $('.menu_main').outerHeight();
            $('.menuModal')
                .removeClass("menuModal_OPEN")
                .css('height', '');
            $body.removeClass("Site_menuOPEN");
            $('.menuTrigger').removeClass("menuTrigger_OPEN");
        }
    }

    return {
        init: function(){
            if (window.innerWidth<991) {
            $(".menuModal").css('height', menuHeight);
            // Меню для мобильных
                $(".menuTrigger").each(function () {
                    $($(this).attr('href')).css('height', 0);
                });
            }

            $(".menuTrigger").click(function(e){
                var $this = $(this),
                    href = $this.attr("href");

                if ($this.hasClass("menuTrigger_OPEN")) {
                    $body.removeClass("Site_menuOPEN");
                    $(href)
                        .removeClass("menuModal_OPEN")
                        .css('height', 0);
                    $this.removeClass("menuTrigger_OPEN");
                }else{
                    $body.addClass("Site_menuOPEN");
                    $(href)
                        .addClass("menuModal_OPEN")
                        .css('height', menuHeight);
                    $this.addClass("menuTrigger_OPEN");
                }
                e.preventDefault();
            });
            $(window).on('resize', refresh);
        }
    };
};
menu().init();


var table = function(){
    return {
        init: function(){
        }
    };
};
table().init();

var API = function(){
    function sendData(address, type, data, cb, $this) {
        $.ajax({
            url: address,
            type: type,
            dataType: 'json',
            data: data,
            complete: function(result) {
                if (result.status===200) {
                    cb(result.responseJSON, $this, data);
                } else {
                    alert('Ошибка ' + result.status);
                }
            }
        });
    }
    
    var send = {
        startIndexing:{
            address: '/startIndexing',
            type: 'GET',
            action: function(result, $this){
                if (result.result){
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').remove();
                    }
                    if ($this.is('[data-btntype="check"]')) {
                        shiftCheck($this);
                    }
                } else {
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').text(result.error);
                    } else {
                        $this.after('<div class="API-error">' + result.error + '</div>');
                    }
                }
            }
        },
        stopIndexing: {
            address: '/stopIndexing',
            type: 'GET',
            action: function(result, $this){
                if (result.result){
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').remove();
                    }
                    if ($this.is('[data-btntype="check"]')) {
                        shiftCheck($this);
                    }
                } else {
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').text(result.error);
                    } else {
                        $this.after('<div class="API-error">' + result.error + '</div>');
                    }
                }
            }
        },
        indexPage: {
            address: '/indexPage',
            type: 'POST',
            action: function(result, $this){
                if (result.result){
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').remove();
                    }
                    if ($this.next('.API-success').length) {
                        $this.next('.API-success').text('Страница добавлена/обновлена успешно');
                    } else {
                        $this.after('<div class="API-success">Страница поставлена в очередь на обновление / добавление</div>');
                    }
                } else {
                    if ($this.next('.API-success').length) {
                        $this.next('.API-success').remove();
                    }
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').text(result.error);
                    } else {
                        $this.after('<div class="API-error">' + result.error + '</div>');
                    }
                }
            }
        },
        search: {
            address: '/search',
            type: 'get',
            action: function(result, $this, data){
                if (result.result){
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').remove();
                    }
                    var $searchResults = $('.SearchResult'),
                        $content = $searchResults.find('.SearchResult-content');
                    if (data.offset === 0) {
                        $content.empty();
                    }
                    $searchResults.find('.SearchResult-amount').text(result.count);
                    var scroll = $(window).scrollTop();
                    result.data.forEach(function(page){
                        $content.append('<div class="SearchResult-block">' +
                            '<a href="' + page.site + page.uri +'" target="_blank" class="SearchResult-siteTitle">' +
                                (!data.siteName ? page.siteName + ' - ': '') +
                                page.title +
                            '</a>' +
                            '<div class="SearchResult-description">' +
                                page.snippet +
                            '</div>' +
                        '</div>')
                    });
                    $(window).scrollTop(scroll);
                    $searchResults.addClass('SearchResult_ACTIVE');
                    if (result.count > data.offset + result.data.length) {
                        $('.SearchResult-footer').removeClass('SearchResult-footer_hide')
                        $('.SearchResult-footer button[data-send="search"]')
                            .data('sendoffset', data.offset + result.data.length)
                            .data('searchquery', data.query)
                            .data('searchsite', data.site)
                            .data('sendlimit', data.limit);
                        var orgText = $searchResults.find('#footerButtonText').text();
                        $('.SearchResult-remain').text(orgText + ' (' + (result.count - data.offset - result.data.length) + ')')
                    } else {
                        $('.SearchResult-footer').addClass('SearchResult-footer_hide')
                    }
                    
                } else {
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').text(result.error);
                    } else {
                        $this.after('<div class="API-error">' + result.error + '</div>');
                    }
                }
            }
        },
        statistics: {
            address: '/statistics',
            type: 'get',
            action: function(result, $this){
                if (result.result){
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').remove();
                    }
    
                    var $statistics = $('.Statistics');
                    $statistics.find('.HideBlock').not('.Statistics-example').remove();
                    $('#totalSites').text(result.statistics.total.sites);
                    $('#totalPages').text(result.statistics.total.pages);
                    $('#totalLemmas').text(result.statistics.total.lemmas);
                    $('select[name="site"] option').not(':first-child').remove();

                    let $hiddenStatusTimeTitle = $('#hiddenStatusTimeTitle').text();
                    let $hiddenPagesTitle = $('#hiddenPagesTitle').text();
                    let $hiddenLemmasTitle = $('#hiddenLemmasTitle').text();
                    let $hiddenErrorTitle = $('#hiddenErrorTitle').text();

                    result.statistics.detailed.forEach(function(site){
                        var $blockSiteExample = $('.Statistics-example').clone(true);
                        var statusClass = '';
                        switch (site.status) {
                            case 'INDEXED':
                                statusClass = 'Statistics-status_checked';
                                break;
                            case 'FAILED':
                                statusClass = 'Statistics-status_cancel';
                                break;
                            case 'INDEXING':
                                statusClass = 'Statistics-status_pause';
                                break;
                            
                        }
                        $('select[name="site"]').append('' +
                            '<option value="' + site.url + '">' +
                                site.url +
                            '</option>')
                        $blockSiteExample.removeClass('Statistics-example');
                        $blockSiteExample.find('.Statistics-status')
                            .addClass(statusClass)
                            .text(site.status)
                            .before(site.name + ' - ' + site.url);
                        var time = new Date(site.statusTime);
                        $blockSiteExample.find('.Statistics-description')
                            .html('<div class="Statistics-option"><strong>' + $hiddenStatusTimeTitle + '</strong> ' +
                                ('00' + time.getDate()).slice(-2) + '.' +
                                ('00' + (time.getMonth() + 1)).slice(-2) + '.' +
                                time.getFullYear() + ' ' +
                                ('00' + time.getHours()).slice(-2) + ':' +
                                ('00' + time.getMinutes()).slice(-2) + ':' +
                                ('00' + time.getSeconds()).slice(-2) +
                            '</div><div class="Statistics-option"><strong>' + $hiddenPagesTitle + '</strong> ' + site.pages +
                            '</div><div class="Statistics-option"><strong>' + $hiddenLemmasTitle + '</strong> ' + site.lemmas +
                            '</div>' +
                            '<div class="Statistics-option Statistics-option_error"><strong>' + $hiddenErrorTitle + '</strong> ' + site.error +
                            '</div>' + '');

                        $statistics.append($blockSiteExample);
                        var $thisHideBlock = $statistics.find('.HideBlock').last();
                        $thisHideBlock.on('click', HideBlock().trigger);


                        $('.Tabs_column > .Tabs-wrap > .Tabs-block').each(function(){
                            var $this = $(this);
                            if ($this.is(':hidden')){
                                $this.addClass('Tabs-block_update')
                            };
                        });
                        $statistics.find('.HideBlock').each(function(){
                            var $this = $(this);
                            var height = $this.find('.Statistics-description').outerHeight();
                            $this.find('.HideBlock-content').css('height', height + 40);
                        });
                        $('.Tabs_column > .Tabs-wrap > .Tabs-block_update').each(function(){
                            var $this = $(this);
                            $this.removeClass('Tabs-block_update')
                        });
                    });
                    if (result.statistics.total.isIndexing) {
                        var $btnIndex = $('.btn[data-send="startIndexing"]'),
                            text = $btnIndex.find('.btn-content').text();
                        $btnIndex.find('.btn-content').text($btnIndex.data('alttext'));
                        $btnIndex
                            .data('check', true)
                            .data('altsend', 'startIndexing')
                            .data('send', 'stopIndexing')
                            .data('alttext', text)
                            .addClass('btn_check')
                        $('.UpdatePageBlock').hide(0)
                    }
    
                } else {
                    if ($this.next('.API-error').length) {
                        $this.next('.API-error').text(result.error);
                    } else {
                        $this.after('<div class="API-error">' + result.error + '</div>');
                    }
                }
                $('.Site-loader').hide(0);
                $('.Site-loadingIsComplete').css('visibility', 'visible').fadeIn(500);
            }
        }
    };
    function shiftCheck($element, wave){
        var text = '',
            check = $element.data('check');
        text = $element.find('.btn-content').text();
        if ($element.data('alttext')) {
            $element.find('.btn-content').text($element.data('alttext'));
            $element.data('alttext', text);
        }
        if ($element.data('send') == 'startIndexing' || $element.data('send') == 'stopIndexing'){
            if (check) {
                $('.UpdatePageBlock').show(0)
            } else {
                $('.UpdatePageBlock').hide(0)
            }
        }
        check = !check;
        $element.data('check', check);
        if ($element.data('altsend')){
            var altsend = $element.data('altsend');
            $element.data('altsend', $element.data('send'));
            $element.data('send', altsend);
        };
        if (check) {
            $element.addClass('btn_check');
        } else {
            $element.removeClass('btn_check');
        };
        if (!wave) {
            $element.trigger('changeCheck');
        }
    }
    return {
        init: function(){
            var $btnCheck = $('[data-btntype="check"]');
            $btnCheck.on('click', function(e){
                var $this = $(this);
                if (!$this.data('send')) {
                    shiftCheck($this);
                }
            });
            $btnCheck.on('changeCheck', function(){
                var $this = $(this);
                if ($this.data('btnradio')) {
                    $('[data-btnradio="' + $this.data('btnradio') + '"]').each(function(e){
                        if($(this).data('check') && !$(this).is($this)) {
                            shiftCheck($(this), true);
                        }
                    });
                }
            });
            sendData(
                send['statistics'].address,
                send['statistics'].type,
                '',
                send['statistics'].action,
                $('.Statistics')
            )
            var $send = $('[data-send]');
            $send.on('submit click', function(e){
                var $this = $(this);
                var data = '';
                if (($this.hasClass('form') && e.type==='submit')
                    || (e.type==='click' && !$this.hasClass('form'))){
                    e.preventDefault();
                    
                    switch ($this.data('send')) {
                        case 'indexPage':
                            var $page = $this.closest('.form').find('input[name="page"]');
                            data = {url: $page.val()};
                            break;
                        case 'search':
                            if ($this.data('sendtype')==='next') {
                                data = {
                                    site: $this.data('searchsite'),
                                    query: $this.data('searchquery'),
                                    offset: $this.data('sendoffset'),
                                    limit: $this.data('sendlimit')
                                };
                            } else {
                                data = {
                                    query: $this.find('[name="query"]').val(),
                                    offset: 0,
                                    limit: $this.data('sendlimit')
                                };
                                if ( $this.find('[name="site"]').val() ) {
                                    data.site = $this.find('[name="site"]').val();
                                }
                            }
                            break;
        
                    }
                    sendData(
                        send[$this.data('send')].address,
                        send[$this.data('send')].type,
                        data,
                        send[$this.data('send')].action,
                        $this
                    )
                }
            });
        }
    };
};
API().init();

var Column = function(){
    return {
        init: function(){
        }
    };
};
Column().init();

var HideBlock = function(){
    var $HideBlock = $('.HideBlock');
    var $trigger = $HideBlock.find('.HideBlock-trigger');
    $HideBlock.each(function(){
        var $this = $(this);
        var $content = $this.find('.HideBlock-content');
        $content.css('height', $content.outerHeight());
        $this.addClass('HideBlock_CLOSE');
    });
    function clickHide (e){
        e.preventDefault();
        var $this = $(this);
        var $parent = $this.closest($HideBlock);
        if ($parent.hasClass('HideBlock_CLOSE')) {
            $('.HideBlock').addClass('HideBlock_CLOSE');
            $parent.removeClass('HideBlock_CLOSE');
        } else {
            $parent.addClass('HideBlock_CLOSE');
        }
    }
    return {
        init: function(){
            $trigger.on('click', clickHide);
            // $HideBlock.eq(0).find($trigger).trigger('click');
        },
        trigger: clickHide
    };
};
HideBlock().init();

var Middle = function(){
    return {
        init: function(){
        }
    };
};
Middle().init();

var SearchResult = function(){
    return {
        init: function(){
        }
    };
};
SearchResult().init();

var Section = function(){
    return {
        init: function(){
        }
    };
};
Section().init();

var Spoiler = function(){
    var $HideBlock = $('.Spoiler');
    var $trigger = $HideBlock.find('.Spoiler-trigger');
    $HideBlock.addClass('Spoiler_CLOSE');
    return {
        init: function(){
            $trigger.on('click', function(e){
                e.preventDefault();
                var $this = $(this);
                var scroll = $(window).scrollTop();
                var $parent = $(this).closest($HideBlock);
                if ($parent.hasClass('Spoiler_CLOSE')) {
                    $parent.removeClass('Spoiler_CLOSE');
                    $(window).scrollTop(scroll);
                } else {
                    $parent.addClass('Spoiler_CLOSE');
                    $(window).scrollTop(scroll);
                }
            });
        }
    };
};
Spoiler().init();

var Statistics = function(){
    return {
        init: function(){
        }
    };
};
Statistics().init();

var Tabs = function(){
    var $tabs = $('.Tabs');
    var $tabsLink = $('.Tabs-link');
    var $tabsBlock = $('.Tabs-block');
    return {
        init: function(){
            $tabsLink.on('click', function(e){
                var $this = $(this);
                var href = $this.attr('href');
                if (href[0]==="#"){
                    e.preventDefault();
                    var $parent = $this.closest($tabs);
                    if ($parent.hasClass('Tabs_steps')) {
                    } else {
                        var $blocks = $parent.find($tabsBlock).not($parent.find($tabs).find($tabsBlock));
                        var $links= $this.add($this.siblings($tabsLink));
                        var $active = $(href);
                        $links.removeClass('Tabs-link_ACTIVE');
                        $this.addClass('Tabs-link_ACTIVE');
                        $blocks.hide(0);
                        $active.show(0);
                    }
                }

            });
            $('.TabsLink').on('click', function(e){
                var $this = $(this);
                var href = $this.attr('href');
                var $active = $(href);
                var $parent = $active.closest($tabs);
                if ($parent.hasClass('Tabs_steps')) {
                } else {
                    var $blocks = $parent.find($tabsBlock).not($parent.find($tabs).find($tabsBlock));
                    var $link = $('.Tabs-link[href="' + href + '"]');
                    var $links= $link.add($link.siblings($tabsLink));
                    $links.removeClass('Tabs-link_ACTIVE');
                    $link.addClass('Tabs-link_ACTIVE');
                    $blocks.hide(0);
                    $active.show(0);
                }

            });
            $tabs.each(function(){
                $(this).find($tabsLink).eq(0).trigger('click');
            });
            if (~window.location.href.indexOf('#')){
                // $(window).scrollTop(0);
                var tab = window.location.href.split('#');
                tab = tab[tab.length - 1];
                $tabsLink.filter('[href="#' + tab + '"]').trigger('click');
            }
            $('.Site').on('click', 'a', function(){
                var $this = $(this),
                    tab = $this.attr('href').replace(window.location.pathname, '');
                if (~$this.attr('href').indexOf(window.location.pathname)) {
                    $tabsLink.filter('[href="' + tab + '"]').trigger('click');
                }
            });
        }
    };
};
Tabs().init();

setTimeout(function(){
    $('body').css('opacity', '1');
}, 100);
});

$('#locales').change(function (){
    var selectedOption = $('#locales').val();
    console.log(selectedOption);
    if(selectedOption != ''){
        window.location.replace('?lang=' + selectedOption);
    }
});


})(jQuery);