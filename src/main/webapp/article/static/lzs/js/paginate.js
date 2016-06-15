/**
 * Created by sunpeng
 */
var baseBtn = 9;
var centerBtn = baseBtn - 4;

var barArr = [];
var container = $('.paginator:eq(0)');
var dots = '...';
function gopage(evnet) {
    var pageNum = evnet.data['pnum'];
    var _page = page;
    switch (pageNum) {
        case 0: //next
            _page++;
            break;
        case -1: // previous
            _page--;
            break;
        default:
            _page = pageNum;
            break;
    }
    if (_page <= pageCnt && _page > 0) {
        if(listUrl.search("-page.") > 0){
            window.location = listUrl.replace("-page.","-"+_page+".");
        }else{
            window.location = listUrl + _page;
        }
    }
}
(function(){

    if(pageCnt == 0){
        return ;
    }

    barArr[0] = $('<a href="javascript: void(0)">上一页</a>');

    var _base = baseBtn;

    barArr[0].bind('click', {pnum: -1}, gopage);
// 上一页 1 2 '3' 4 5 6 7 8 9 下一页
    if (pageCnt <= (_base + 1)) { // _base + 1 => (上一页 1 2 '3' 4 5 6 7 8 9 10 下一页)

        for (var i = 1; i <= pageCnt; i++) {
            if (i != page) {
                barArr[i] = $('<a href="javascript: void(0)">' + i + '</a>');
                barArr[i].bind('click', {pnum: i}, gopage);
            } else {
                barArr[i] = $('<span class= "current" >' + i + '</span>');
            }
        }

    } else {
        _base = (_base + 1) / 2 + 1;
        if (page <= _base) {
            // 上一页 1 2 3 4 5 '6' 7 ... xx 下一页
            for (var i = 1; i <= (_base + 1); i++) {
                if (i != page) {
                    barArr[i] = $('<a href="javascript: void(0)">' + i + '</a>');
                    barArr[i].bind('click', {pnum: i}, gopage);
                } else {
                    barArr[i] = $('<span class= "current" >' + i + '</span>');
                }
            }
            barArr[barArr.length] = dots;

            barArr[barArr.length] = $('<a href="javascript: void(0)">' + pageCnt + '</a>');
            barArr[barArr.length - 1].bind('click', {pnum: pageCnt}, gopage);

        } else {

            barArr[1] = $('<a href="javascript: void(0)">' + 1 + '</a>');
            barArr[1].bind('click', {pnum: 1}, gopage);

            barArr[2] = dots;
            var starIndex = 3;

            var remainPage = pageCnt - page;
            var offset = pageCnt - baseBtn;
            if (remainPage < (baseBtn + 1) / 2) {
                // 上一页 1 ... 5 6 '7' 8 9 10 11 下一页
                for (var i = starIndex; i < baseBtn; i++) {
                    if ((i + offset) == page) {
                        barArr[i] = $('<span class= "current" >' + (i + offset) + '</span>');
                    } else {
                        barArr[i] = $('<a href="javascript: void(0)">' + (i + offset) + '</a>');
                        barArr[i].bind('click', {pnum: (i + offset)}, gopage);
                    }
                }

            } else {
                offset = page - (baseBtn + 1) / 2;
                // 上一页 1 ... 5 6 '7' 8 9 ... xx 下一页
                for (var i = starIndex; i < (centerBtn + starIndex); i++) {
                    if ((i + offset) != page) {
                        barArr[i] = $('<a href="javascript: void(0)">' + (i + offset) + '</a>');
                        barArr[i].bind('click', {pnum: (i + offset)}, gopage);
                    } else {
                        barArr[i] = $('<span class= "current" >' + (i + offset) + '</span>');
                    }
                }

                barArr[barArr.length] = dots;

            }
            if (page == pageCnt) {
                barArr[i] = $('<span class= "current" >' + page + '</span>');
            } else {
                barArr[barArr.length] = $('<a href="javascript: void(0)">' + pageCnt + '</a>');
                barArr[barArr.length - 1].bind('click', {pnum: pageCnt}, gopage);
            }
        }
    }

    barArr[barArr.length] = $('<a href="javascript: void(0)">下一页</a>');
    barArr[barArr.length - 1].bind('click', {pnum: 0}, gopage);

    for (var i = 0; i < barArr.length; i++) {
        container.append(barArr[i]);
    }
})()