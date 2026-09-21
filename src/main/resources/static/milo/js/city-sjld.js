(function() {
    // ------------------------------------------------------------
    // 数据源：四川省全部区县
    // ------------------------------------------------------------
    const rawData = [
        { province: '四川省', city: '成都市', district: '锦江区', code: '510104' },
        { province: '四川省', city: '成都市', district: '青羊区', code: '510105' },
        { province: '四川省', city: '成都市', district: '金牛区', code: '510106' },
        { province: '四川省', city: '成都市', district: '武侯区', code: '510107' },
        { province: '四川省', city: '成都市', district: '成华区', code: '510108' },
        { province: '四川省', city: '成都市', district: '龙泉驿区', code: '510112' },
        { province: '四川省', city: '成都市', district: '青白江区', code: '510113' },
        { province: '四川省', city: '成都市', district: '新都区', code: '510114' },
        { province: '四川省', city: '成都市', district: '温江区', code: '510115' },
        { province: '四川省', city: '成都市', district: '双流区', code: '510116' },
        { province: '四川省', city: '成都市', district: '郫都区', code: '510117' },
        { province: '四川省', city: '成都市', district: '新津区', code: '510118' },
        { province: '四川省', city: '成都市', district: '简阳市', code: '510185' },
        { province: '四川省', city: '成都市', district: '都江堰市', code: '510181' },
        { province: '四川省', city: '成都市', district: '彭州市', code: '510182' },
        { province: '四川省', city: '成都市', district: '邛崃市', code: '510183' },
        { province: '四川省', city: '成都市', district: '崇州市', code: '510184' },
        { province: '四川省', city: '成都市', district: '金堂县', code: '510121' },
        { province: '四川省', city: '成都市', district: '大邑县', code: '510129' },
        { province: '四川省', city: '成都市', district: '蒲江县', code: '510131' },
        { province: '四川省', city: '自贡市', district: '自流井区', code: '510302' },
        { province: '四川省', city: '自贡市', district: '贡井区', code: '510303' },
        { province: '四川省', city: '自贡市', district: '大安区', code: '510304' },
        { province: '四川省', city: '自贡市', district: '沿滩区', code: '510311' },
        { province: '四川省', city: '自贡市', district: '荣县', code: '510321' },
        { province: '四川省', city: '自贡市', district: '富顺县', code: '510322' },
        { province: '四川省', city: '攀枝花市', district: '东区', code: '510402' },
        { province: '四川省', city: '攀枝花市', district: '西区', code: '510403' },
        { province: '四川省', city: '攀枝花市', district: '仁和区', code: '510411' },
        { province: '四川省', city: '攀枝花市', district: '米易县', code: '510421' },
        { province: '四川省', city: '攀枝花市', district: '盐边县', code: '510422' },
        { province: '四川省', city: '泸州市', district: '江阳区', code: '510502' },
        { province: '四川省', city: '泸州市', district: '纳溪区', code: '510503' },
        { province: '四川省', city: '泸州市', district: '龙马潭区', code: '510504' },
        { province: '四川省', city: '泸州市', district: '泸县', code: '510521' },
        { province: '四川省', city: '泸州市', district: '合江县', code: '510522' },
        { province: '四川省', city: '泸州市', district: '叙永县', code: '510524' },
        { province: '四川省', city: '泸州市', district: '古蔺县', code: '510525' },
        { province: '四川省', city: '德阳市', district: '旌阳区', code: '510603' },
        { province: '四川省', city: '德阳市', district: '罗江区', code: '510604' },
        { province: '四川省', city: '德阳市', district: '广汉市', code: '510681' },
        { province: '四川省', city: '德阳市', district: '什邡市', code: '510682' },
        { province: '四川省', city: '德阳市', district: '绵竹市', code: '510683' },
        { province: '四川省', city: '德阳市', district: '中江县', code: '510623' },
        { province: '四川省', city: '绵阳市', district: '涪城区', code: '510703' },
        { province: '四川省', city: '绵阳市', district: '游仙区', code: '510704' },
        { province: '四川省', city: '绵阳市', district: '安州区', code: '510705' },
        { province: '四川省', city: '绵阳市', district: '江油市', code: '510781' },
        { province: '四川省', city: '绵阳市', district: '三台县', code: '510722' },
        { province: '四川省', city: '绵阳市', district: '盐亭县', code: '510723' },
        { province: '四川省', city: '绵阳市', district: '梓潼县', code: '510725' },
        { province: '四川省', city: '绵阳市', district: '平武县', code: '510727' },
        { province: '四川省', city: '绵阳市', district: '北川羌族自治县', code: '510726' },
        { province: '四川省', city: '广元市', district: '利州区', code: '510802' },
        { province: '四川省', city: '广元市', district: '昭化区', code: '510811' },
        { province: '四川省', city: '广元市', district: '朝天区', code: '510812' },
        { province: '四川省', city: '广元市', district: '旺苍县', code: '510821' },
        { province: '四川省', city: '广元市', district: '青川县', code: '510822' },
        { province: '四川省', city: '广元市', district: '剑阁县', code: '510823' },
        { province: '四川省', city: '广元市', district: '苍溪县', code: '510824' },
        { province: '四川省', city: '遂宁市', district: '船山区', code: '510903' },
        { province: '四川省', city: '遂宁市', district: '安居区', code: '510904' },
        { province: '四川省', city: '遂宁市', district: '射洪市', code: '510981' },
        { province: '四川省', city: '遂宁市', district: '蓬溪县', code: '510921' },
        { province: '四川省', city: '遂宁市', district: '大英县', code: '510923' },
        { province: '四川省', city: '内江市', district: '市中区', code: '511002' },
        { province: '四川省', city: '内江市', district: '东兴区', code: '511011' },
        { province: '四川省', city: '内江市', district: '隆昌市', code: '511083' },
        { province: '四川省', city: '内江市', district: '威远县', code: '511024' },
        { province: '四川省', city: '内江市', district: '资中县', code: '511025' },
        { province: '四川省', city: '乐山市', district: '市中区', code: '511102' },
        { province: '四川省', city: '乐山市', district: '沙湾区', code: '511111' },
        { province: '四川省', city: '乐山市', district: '五通桥区', code: '511112' },
        { province: '四川省', city: '乐山市', district: '金口河区', code: '511113' },
        { province: '四川省', city: '乐山市', district: '峨眉山市', code: '511181' },
        { province: '四川省', city: '乐山市', district: '犍为县', code: '511123' },
        { province: '四川省', city: '乐山市', district: '井研县', code: '511124' },
        { province: '四川省', city: '乐山市', district: '夹江县', code: '511126' },
        { province: '四川省', city: '乐山市', district: '沐川县', code: '511129' },
        { province: '四川省', city: '乐山市', district: '峨边彝族自治县', code: '511132' },
        { province: '四川省', city: '乐山市', district: '马边彝族自治县', code: '511133' },
        { province: '四川省', city: '南充市', district: '顺庆区', code: '511302' },
        { province: '四川省', city: '南充市', district: '高坪区', code: '511303' },
        { province: '四川省', city: '南充市', district: '嘉陵区', code: '511304' },
        { province: '四川省', city: '南充市', district: '阆中市', code: '511381' },
        { province: '四川省', city: '南充市', district: '南部县', code: '511321' },
        { province: '四川省', city: '南充市', district: '营山县', code: '511322' },
        { province: '四川省', city: '南充市', district: '蓬安县', code: '511323' },
        { province: '四川省', city: '南充市', district: '仪陇县', code: '511324' },
        { province: '四川省', city: '南充市', district: '西充县', code: '511325' },
        { province: '四川省', city: '眉山市', district: '东坡区', code: '511402' },
        { province: '四川省', city: '眉山市', district: '彭山区', code: '511403' },
        { province: '四川省', city: '眉山市', district: '仁寿县', code: '511421' },
        { province: '四川省', city: '眉山市', district: '洪雅县', code: '511423' },
        { province: '四川省', city: '眉山市', district: '丹棱县', code: '511424' },
        { province: '四川省', city: '眉山市', district: '青神县', code: '511425' },
        { province: '四川省', city: '宜宾市', district: '翠屏区', code: '511502' },
        { province: '四川省', city: '宜宾市', district: '南溪区', code: '511503' },
        { province: '四川省', city: '宜宾市', district: '叙州区', code: '511504' },
        { province: '四川省', city: '宜宾市', district: '江安县', code: '511523' },
        { province: '四川省', city: '宜宾市', district: '长宁县', code: '511524' },
        { province: '四川省', city: '宜宾市', district: '高县', code: '511525' },
        { province: '四川省', city: '宜宾市', district: '珙县', code: '511526' },
        { province: '四川省', city: '宜宾市', district: '筠连县', code: '511527' },
        { province: '四川省', city: '宜宾市', district: '兴文县', code: '511528' },
        { province: '四川省', city: '宜宾市', district: '屏山县', code: '511529' },
        { province: '四川省', city: '广安市', district: '广安区', code: '511602' },
        { province: '四川省', city: '广安市', district: '前锋区', code: '511603' },
        { province: '四川省', city: '广安市', district: '华蓥市', code: '511681' },
        { province: '四川省', city: '广安市', district: '岳池县', code: '511621' },
        { province: '四川省', city: '广安市', district: '武胜县', code: '511622' },
        { province: '四川省', city: '广安市', district: '邻水县', code: '511623' },
        { province: '四川省', city: '达州市', district: '通川区', code: '511702' },
        { province: '四川省', city: '达州市', district: '达川区', code: '511703' },
        { province: '四川省', city: '达州市', district: '万源市', code: '511781' },
        { province: '四川省', city: '达州市', district: '宣汉县', code: '511722' },
        { province: '四川省', city: '达州市', district: '开江县', code: '511723' },
        { province: '四川省', city: '达州市', district: '大竹县', code: '511724' },
        { province: '四川省', city: '达州市', district: '渠县', code: '511725' },
        { province: '四川省', city: '雅安市', district: '雨城区', code: '511802' },
        { province: '四川省', city: '雅安市', district: '名山区', code: '511803' },
        { province: '四川省', city: '雅安市', district: '荥经县', code: '511822' },
        { province: '四川省', city: '雅安市', district: '汉源县', code: '511823' },
        { province: '四川省', city: '雅安市', district: '石棉县', code: '511824' },
        { province: '四川省', city: '雅安市', district: '天全县', code: '511825' },
        { province: '四川省', city: '雅安市', district: '芦山县', code: '511826' },
        { province: '四川省', city: '雅安市', district: '宝兴县', code: '511827' },
        { province: '四川省', city: '巴中市', district: '巴州区', code: '511902' },
        { province: '四川省', city: '巴中市', district: '恩阳区', code: '511903' },
        { province: '四川省', city: '巴中市', district: '通江县', code: '511921' },
        { province: '四川省', city: '巴中市', district: '南江县', code: '511922' },
        { province: '四川省', city: '巴中市', district: '平昌县', code: '511923' },
        { province: '四川省', city: '资阳市', district: '雁江区', code: '512002' },
        { province: '四川省', city: '资阳市', district: '安岳县', code: '512021' },
        { province: '四川省', city: '资阳市', district: '乐至县', code: '512022' },
        { province: '四川省', city: '阿坝州', district: '马尔康市', code: '513201' },
        { province: '四川省', city: '阿坝州', district: '汶川县', code: '513221' },
        { province: '四川省', city: '阿坝州', district: '理县', code: '513222' },
        { province: '四川省', city: '阿坝州', district: '茂县', code: '513223' },
        { province: '四川省', city: '阿坝州', district: '松潘县', code: '513224' },
        { province: '四川省', city: '阿坝州', district: '九寨沟县', code: '513225' },
        { province: '四川省', city: '阿坝州', district: '金川县', code: '513226' },
        { province: '四川省', city: '阿坝州', district: '小金县', code: '513227' },
        { province: '四川省', city: '阿坝州', district: '黑水县', code: '513228' },
        { province: '四川省', city: '阿坝州', district: '壤塘县', code: '513230' },
        { province: '四川省', city: '阿坝州', district: '阿坝县', code: '513231' },
        { province: '四川省', city: '阿坝州', district: '若尔盖县', code: '513232' },
        { province: '四川省', city: '阿坝州', district: '红原县', code: '513233' },
        { province: '四川省', city: '甘孜州', district: '康定市', code: '513301' },
        { province: '四川省', city: '甘孜州', district: '泸定县', code: '513322' },
        { province: '四川省', city: '甘孜州', district: '丹巴县', code: '513323' },
        { province: '四川省', city: '甘孜州', district: '九龙县', code: '513324' },
        { province: '四川省', city: '甘孜州', district: '雅江县', code: '513325' },
        { province: '四川省', city: '甘孜州', district: '道孚县', code: '513326' },
        { province: '四川省', city: '甘孜州', district: '炉霍县', code: '513327' },
        { province: '四川省', city: '甘孜州', district: '甘孜县', code: '513328' },
        { province: '四川省', city: '甘孜州', district: '新龙县', code: '513329' },
        { province: '四川省', city: '甘孜州', district: '德格县', code: '513330' },
        { province: '四川省', city: '甘孜州', district: '白玉县', code: '513331' },
        { province: '四川省', city: '甘孜州', district: '石渠县', code: '513332' },
        { province: '四川省', city: '甘孜州', district: '色达县', code: '513333' },
        { province: '四川省', city: '甘孜州', district: '理塘县', code: '513334' },
        { province: '四川省', city: '甘孜州', district: '巴塘县', code: '513335' },
        { province: '四川省', city: '甘孜州', district: '乡城县', code: '513336' },
        { province: '四川省', city: '甘孜州', district: '稻城县', code: '513337' },
        { province: '四川省', city: '甘孜州', district: '得荣县', code: '513338' },
        { province: '四川省', city: '凉山州', district: '西昌市', code: '513401' },
        { province: '四川省', city: '凉山州', district: '会理市', code: '513402' },
        { province: '四川省', city: '凉山州', district: '木里藏族自治县', code: '513422' },
        { province: '四川省', city: '凉山州', district: '盐源县', code: '513423' },
        { province: '四川省', city: '凉山州', district: '德昌县', code: '513424' },
        { province: '四川省', city: '凉山州', district: '会东县', code: '513426' },
        { province: '四川省', city: '凉山州', district: '宁南县', code: '513427' },
        { province: '四川省', city: '凉山州', district: '普格县', code: '513428' },
        { province: '四川省', city: '凉山州', district: '布拖县', code: '513429' },
        { province: '四川省', city: '凉山州', district: '金阳县', code: '513430' },
        { province: '四川省', city: '凉山州', district: '昭觉县', code: '513431' },
        { province: '四川省', city: '凉山州', district: '喜德县', code: '513432' },
        { province: '四川省', city: '凉山州', district: '冕宁县', code: '513433' },
        { province: '四川省', city: '凉山州', district: '越西县', code: '513434' },
        { province: '四川省', city: '凉山州', district: '甘洛县', code: '513435' },
        { province: '四川省', city: '凉山州', district: '美姑县', code: '513436' },
        { province: '四川省', city: '凉山州', district: '雷波县', code: '513437' },
    ];

    // ------------------------------------------------------------
    // DOM 引用
    const provinceSelect = document.getElementById('provinceSelect');
    const cityInput = document.getElementById('cityInput');
    const cityDropdown = document.getElementById('cityDropdown');
    const cityClearBtn = document.getElementById('cityClearBtn');
    const districtInput = document.getElementById('districtInput');
    const districtDropdown = document.getElementById('districtDropdown');
    const districtClearBtn = document.getElementById('districtClearBtn');
    const resetBtn = document.getElementById('resetBtn');
    const tbody = document.getElementById('tableBody');
    const totalSpan = document.getElementById('totalCount');

    // 状态
    let currentProvince = '四川省';
    let currentCity = '';
    let currentDistrict = '';

    // 所有城市列表（缓存）
    let allCities = [];
    // 当前城市对应的区县列表（缓存）
    let currentDistricts = [];

    // ------------------------------------------------------------
    // 工具函数
    function getCities(province) {
        const citySet = new Set();
        rawData.forEach(item => {
            if (item.province === province) {
                citySet.add(item.city);
            }
        });
        return Array.from(citySet);
    }

    function getDistricts(province, city) {
        const result = [];
        rawData.forEach(item => {
            if (item.province === province && item.city === city) {
                result.push(item);
            }
        });
        return result;
    }

    // ------------------------------------------------------------
    // 清除按钮显示控制
    function updateClearBtnVisibility(input, clearBtn) {
        if (input.value && input.value.trim() !== '' && !input.disabled) {
            clearBtn.classList.add('visible');
        } else {
            clearBtn.classList.remove('visible');
        }
    }

    // ------------------------------------------------------------
    // 渲染下拉选项
    function renderCityDropdown(filterText) {
        const keyword = filterText ? filterText.trim() : '';
        let matched = allCities;
        if (keyword) {
            matched = allCities.filter(c => c.includes(keyword));
        }

        cityDropdown.innerHTML = '';
        if (matched.length === 0) {
            const div = document.createElement('div');
            div.className = 'option-item no-match';
            div.textContent = '无匹配城市';
            cityDropdown.appendChild(div);
        } else {
            matched.forEach(city => {
                const div = document.createElement('div');
                div.className = 'option-item';
                div.textContent = city;
                div.dataset.value = city;
                div.addEventListener('click', function() {
                    selectCity(city);
                });
                cityDropdown.appendChild(div);
            });
        }
        cityDropdown.classList.add('show');
    }

    function renderDistrictDropdown(filterText) {
        const keyword = filterText ? filterText.trim() : '';
        let matched = currentDistricts;
        if (keyword) {
            matched = currentDistricts.filter(d => d.district.includes(keyword));
        }

        districtDropdown.innerHTML = '';
        if (matched.length === 0) {
            const div = document.createElement('div');
            div.className = 'option-item no-match';
            div.textContent = '无匹配区县';
            districtDropdown.appendChild(div);
        } else {
            matched.forEach(item => {
                const div = document.createElement('div');
                div.className = 'option-item';
                div.textContent = item.district;
                div.dataset.value = item.district;
                div.addEventListener('click', function() {
                    selectDistrict(item.district);
                });
                districtDropdown.appendChild(div);
            });
        }
        districtDropdown.classList.add('show');
    }

    // ------------------------------------------------------------
    // 选择城市
    function selectCity(city) {
        currentCity = city;
        cityInput.value = city;
        cityDropdown.classList.remove('show');
        updateClearBtnVisibility(cityInput, cityClearBtn);

        // 更新区县列表
        currentDistricts = getDistricts(currentProvince, city);
        currentDistrict = '';
        districtInput.value = '';
        districtInput.disabled = false;
        updateClearBtnVisibility(districtInput, districtClearBtn);
        renderDistrictDropdown('');

        renderTable();
    }

    // ------------------------------------------------------------
    // 选择区县
    function selectDistrict(district) {
        currentDistrict = district;
        districtInput.value = district;
        districtDropdown.classList.remove('show');
        updateClearBtnVisibility(districtInput, districtClearBtn);
        renderTable();
    }

    // ------------------------------------------------------------
    // 清除城市
    function clearCity() {
        currentCity = '';
        cityInput.value = '';
        cityDropdown.classList.remove('show');
        updateClearBtnVisibility(cityInput, cityClearBtn);
        // 清空区县
        currentDistricts = [];
        currentDistrict = '';
        districtInput.value = '';
        districtInput.disabled = true;
        updateClearBtnVisibility(districtInput, districtClearBtn);
        districtDropdown.classList.remove('show');
        renderTable();
    }

    // ------------------------------------------------------------
    // 清除区县
    function clearDistrict() {
        currentDistrict = '';
        districtInput.value = '';
        districtDropdown.classList.remove('show');
        updateClearBtnVisibility(districtInput, districtClearBtn);
        renderTable();
    }

    // ------------------------------------------------------------
    // 渲染表格（只有市选中时才显示数据）
    function renderTable() {
        const province = currentProvince;
        const city = currentCity;
        const district = currentDistrict;

        let filtered = [];
        if (province && city && district) {
            filtered = rawData.filter(item =>
                item.province === province &&
                item.city === city &&
                item.district === district
            );
        } else if (province && city) {
            filtered = rawData.filter(item =>
                item.province === province &&
                item.city === city
            );
        } else {
            filtered = [];
        }

        let html = '';
        if (filtered.length === 0) {
            html = `<tr class="empty-row"><td colspan="4">? 请选择市、区县进行筛选</td></tr>`;
        } else {
            filtered.forEach((item, index) => {
                html += `<tr>
                        <td class="col-seq">${index + 1}</td>
                        <td class="col-city">${item.city}</td>
                        <td class="col-district">${item.district}</td>
                        <td class="col-code">${item.code}</td>
                    </tr>`;
            });
        }
        tbody.innerHTML = html;
        totalSpan.textContent = filtered.length;
    }

    // ------------------------------------------------------------
    // 清空城市和区县
    function clearCityAndDistrict() {
        currentCity = '';
        cityInput.value = '';
        cityInput.disabled = true;
        updateClearBtnVisibility(cityInput, cityClearBtn);
        currentDistricts = [];
        currentDistrict = '';
        districtInput.value = '';
        districtInput.disabled = true;
        updateClearBtnVisibility(districtInput, districtClearBtn);
        districtDropdown.classList.remove('show');
        cityDropdown.classList.remove('show');
    }

    // ------------------------------------------------------------
    // 初始化城市列表（省默认选中，但市和区县为空）
    function initCityList(province) {
        currentProvince = province;
        allCities = getCities(province);
        cityInput.disabled = false;
        cityInput.value = '';
        currentCity = '';
        updateClearBtnVisibility(cityInput, cityClearBtn);
        currentDistricts = [];
        currentDistrict = '';
        districtInput.value = '';
        districtInput.disabled = true;
        updateClearBtnVisibility(districtInput, districtClearBtn);
        cityDropdown.classList.remove('show');
        districtDropdown.classList.remove('show');
        renderTable();
    }

    // ------------------------------------------------------------
    // 事件绑定：城市清除按钮
    cityClearBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        clearCity();
        // 聚焦到输入框方便重新输入
        cityInput.focus();
    });

    // ------------------------------------------------------------
    // 事件绑定：区县清除按钮
    districtClearBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        clearDistrict();
        districtInput.focus();
    });

    // ------------------------------------------------------------
    // 事件绑定：城市输入框
    cityInput.addEventListener('input', function() {
        const val = this.value;
        updateClearBtnVisibility(this, cityClearBtn);
        if (currentProvince) {
            renderCityDropdown(val);
        }
    });

    cityInput.addEventListener('focus', function() {
        if (!cityInput.disabled && currentProvince) {
            renderCityDropdown(this.value);
        }
    });

    // 点击外部关闭下拉
    document.addEventListener('click', function(e) {
        const wrapper = document.getElementById('cityWrapper');
        if (!wrapper.contains(e.target)) {
            cityDropdown.classList.remove('show');
        }
        const wrapper2 = document.getElementById('districtWrapper');
        if (!wrapper2.contains(e.target)) {
            districtDropdown.classList.remove('show');
        }
    });

    // 城市键盘事件
    cityInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            const val = this.value.trim();
            if (val && allCities.includes(val)) {
                selectCity(val);
            } else {
                const matched = allCities.filter(c => c.includes(val));
                if (matched.length > 0) {
                    selectCity(matched[0]);
                }
            }
            e.preventDefault();
        }
        if (e.key === 'Escape') {
            cityDropdown.classList.remove('show');
            this.blur();
        }
    });

    // ------------------------------------------------------------
    // 事件绑定：区县输入框
    districtInput.addEventListener('input', function() {
        const val = this.value;
        updateClearBtnVisibility(this, districtClearBtn);
        if (currentProvince && currentCity) {
            renderDistrictDropdown(val);
        }
    });

    districtInput.addEventListener('focus', function() {
        if (!districtInput.disabled && currentProvince && currentCity) {
            renderDistrictDropdown(this.value);
        }
    });

    districtInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            const val = this.value.trim();
            if (val) {
                const matched = currentDistricts.filter(d => d.district.includes(val));
                if (matched.length > 0) {
                    selectDistrict(matched[0].district);
                }
            }
            e.preventDefault();
        }
        if (e.key === 'Escape') {
            districtDropdown.classList.remove('show');
            this.blur();
        }
    });

    // ------------------------------------------------------------
    // 省份切换（目前只有四川省，但保留逻辑）
    provinceSelect.addEventListener('change', function() {
        const province = this.value;
        currentProvince = province;

        if (province) {
            allCities = getCities(province);
            cityInput.disabled = false;
            cityInput.value = '';
            currentCity = '';
            updateClearBtnVisibility(cityInput, cityClearBtn);
            currentDistricts = [];
            currentDistrict = '';
            districtInput.value = '';
            districtInput.disabled = true;
            updateClearBtnVisibility(districtInput, districtClearBtn);
            districtDropdown.classList.remove('show');
            renderTable();
        } else {
            allCities = [];
            clearCityAndDistrict();
            renderTable();
        }
        cityDropdown.classList.remove('show');
        districtDropdown.classList.remove('show');
    });

    // ------------------------------------------------------------
    // 重置按钮：清空市和区县，省保持不变（四川省）
    resetBtn.addEventListener('click', function() {
        provinceSelect.value = '四川省';
        currentProvince = '四川省';
        allCities = getCities('四川省');
        clearCityAndDistrict();
        cityInput.disabled = false;
        updateClearBtnVisibility(cityInput, cityClearBtn);
        cityDropdown.classList.remove('show');
        districtDropdown.classList.remove('show');
        renderTable();
    });

    // ------------------------------------------------------------
    // 初始化：省默认选中四川省，市和区县为空
    provinceSelect.value = '四川省';
    currentProvince = '四川省';
    allCities = getCities('四川省');
    cityInput.disabled = false;
    cityInput.value = '';
    currentCity = '';
    updateClearBtnVisibility(cityInput, cityClearBtn);
    districtInput.disabled = true;
    districtInput.value = '';
    updateClearBtnVisibility(districtInput, districtClearBtn);
    currentDistricts = [];
    currentDistrict = '';
    renderTable();

    console.log('? 省市区三级联动已启动，省默认选中四川省，带清除按钮');
})();