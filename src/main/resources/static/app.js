// API 기본 URL
const API_BASE_URL = '/api/portfolio';

// 전역 변수
let timeSeriesChart = null;
let comparisonChart = null;
let amountChart = null;

// DOM 요소
const portfolioForm = document.getElementById('portfolioForm');
const loadingSpinner = document.getElementById('loadingSpinner');
const resultsSection = document.getElementById('resultsSection');
const errorSection = document.getElementById('errorSection');
const errorMessage = document.getElementById('errorMessage');

// 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', function () {
    portfolioForm.addEventListener('submit', handleFormSubmit);

    // 주식 심볼 입력 시 가중치 컨테이너 업데이트
    document.getElementById('tickers').addEventListener('input', updateWeightsContainer);

    // 기본 날짜 설정
    const today = new Date();
    const oneYearAgo = new Date(today.getFullYear() - 1, today.getMonth(), today.getDate());

    //    document.getElementById('startDate').value = oneYearAgo.toISOString().split('T')[0];
    //document.getElementById('endDate').value = today.toISOString().split('T')[0];

    // 초기 가중치 컨테이너 업데이트
    updateWeightsContainer();
});

// 폼 제출 처리
async function handleFormSubmit(event) {
    event.preventDefault();

    const formData = getFormData();
    if (!validateFormData(formData)) {
        return;
    }

    showLoading();
    hideError();
    hideResults();

    try {
        // 통합 API로 모든 데이터 한 번에 호출
        const analysisResult = await analyzePortfolioAll(formData);
        await displayResultsAll(analysisResult);
        showResults();
    } catch (error) {
        console.error('Analysis failed:', error);
        if (error.message.startsWith('Stock data has different start dates')) {
            const suggestedDate = error.message.split('The latest start date is ')[1].replace('.', '');
            const userMessage = `선택한 주식들의 데이터 시작일이 다릅니다. 모든 데이터를 포함하려면 시작일을 ${suggestedDate} 이후로 설정해주세요.`;
            showError(userMessage);
            document.getElementById('startDate').value = suggestedDate;
        } else {
            showError(error.message || '포트폴리오 분석 중 오류가 발생했습니다.');
        }
    } finally {
        hideLoading();
    }
}

// 통합 결과 표시 함수
async function displayResultsAll(analysisResult) {
    // 1. 포트폴리오 요약 표시
    displayPortfolioSummary(analysisResult.portfolioData);

    // 2. 차트 생성 (통합 데이터 사용)
    await createChartsAll(analysisResult);

    // 3. 개별 주식 분석 테이블
    displayStockAnalysisTable(analysisResult.portfolioData);

    // 4. 리스크 지표 표시
    displayRiskMetrics(analysisResult.portfolioData);
}

// 통합 차트 생성 함수
async function createChartsAll(analysisResult) {
    // 시계열 차트
    if (analysisResult.timeSeriesChart) {
        createTimeSeriesChart(analysisResult.timeSeriesChart);
    }
    // 비교 차트
    if (analysisResult.comparisonChart) {
        createComparisonChart(analysisResult.comparisonChart);
    }
    // 금액 변화 차트
    if (analysisResult.amountChart) {
        createAmountChartFromData(analysisResult.amountChart);
        document.getElementById('amountChartSection').style.display = 'block';
    } else {
        document.getElementById('amountChartSection').style.display = 'none';
    }
    // 최대 낙폭(MDD) 차트
    if (analysisResult.portfolioData
        && analysisResult.portfolioData.portfolioStockReturn
        && analysisResult.portfolioData.portfolioStockReturn.maxDrawdowns 
        && analysisResult.portfolioData.portfolioStockReturn.maxDrawdowns.length > 0) {
        createMaxDrawdownChart(analysisResult.portfolioData.portfolioStockReturn);
        document.getElementById('maxDrawdownChartSection').style.display = 'block';
    } else {
        document.getElementById('maxDrawdownChartSection').style.display = 'none';
    }
}


// 폼 데이터 수집
function normalizeMonthInput(value) {
    // YYYY-MM이면 YYYY-MM-01로 변환, 아니면 그대로 반환
    return /^\d{4}-\d{2}$/.test(value) ? value + "-01" : value;
}

function getFormData() {
    const tickers = document.getElementById('tickers').value
        .split(',')
        .map(ticker => ticker.trim().toUpperCase())
        .filter(ticker => ticker.length > 0);

    const weights = getWeightsData();
    const initialAmount = parseFloat(document.getElementById('initialAmount').value) || 0;

    return {
        tickers: tickers,
        weights: weights.length > 0 ? weights : null, // 가중치가 있을 때만 포함
        startDate: normalizeMonthInput(document.getElementById('startDate').value),
        endDate: normalizeMonthInput(document.getElementById('endDate').value),
        includeDividends: document.getElementById('includeDividends').checked,
        initialAmount: initialAmount
    };
}

// 폼 데이터 검증
function validateFormData(formData) {
    if (formData.tickers.length === 0) {
        showError('최소 하나의 주식 심볼을 입력해주세요.');
        return false;
    }

    if (!formData.startDate || !formData.endDate) {
        showError('시작일과 종료일을 모두 입력해주세요.');
        return false;
    }

    if (new Date(formData.startDate) >= new Date(formData.endDate)) {
        showError('시작일은 종료일보다 이전이어야 합니다.');
        return false;
    }

    return true;
}

// 통합 분석/차트/리포트 API 호출
async function analyzePortfolioAll(formData) {
    const response = await fetch(`${API_BASE_URL}/analyze/all`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData)
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
}


// 포트폴리오 요약 표시
function displayPortfolioSummary(portfolioData) {
    let portfolio = portfolioData.portfolioStockReturn;
    const summaryContainer = document.getElementById('portfolioSummary');

    const metrics = [
        {
            label: '포트폴리오 가격 수익률',
            value: formatPercentage(portfolio.priceReturn),
            class: getReturnClass(portfolio.priceReturn)
        },
        {
            label: '포트폴리오 총 수익률',
            value: formatPercentage(portfolio.totalReturn),
            class: getReturnClass(portfolio.totalReturn)
        },
        {
            label: '연평균 성장률 (CAGR)',
            value: formatPercentage(portfolio.cagr),
            class: getReturnClass(portfolio.cagr)
        },
        {
            label: '변동성',
            value: formatPercentage(portfolio.volatility),
            class: 'neutral-return'
        }
    ];

    // 자연어 요약 문구 생성
    const summaryText = `이 포트폴리오는 ${portfolioData.startDate}부터 ${portfolioData.endDate}까지의 기간 동안 ` +
        `총수익률은 ${formatPercentage(portfolio.totalReturn)}, ` +
        `연평균 성장률은 ${formatPercentage(portfolio.cagr)}, ` +
        `변동성은 ${formatPercentage(portfolio.volatility)}입니다.`;

    summaryContainer.innerHTML = `
        <div class="portfolio-summary-text" style="font-size:1.1em; margin-bottom: 16px; font-weight:500; color:#333;">
            ${summaryText}
        </div>
    ` + metrics.map(metric => `
        <div class="col-md-3 col-sm-6">
            <div class="metric-card">
                <div class="metric-value ${metric.class}">${metric.value}</div>
                <div class="metric-label">${metric.label}</div>
            </div>
        </div>
    `).join('');
}

// 최대 낙폭(MDD) 차트 생성
function createMaxDrawdownChart(stockReturnData) {
    const ctx = document.getElementById('maxDrawdownChart').getContext('2d');
    if (window.maxDrawdownChartInstance) {
        window.maxDrawdownChartInstance.destroy();
    }
    window.maxDrawdownChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: (stockReturnData.dates || []).map(formatDate),
            datasets: [{
                label: '최대 낙폭(MDD)',
                data: stockReturnData.maxDrawdowns || [],
                borderColor: 'rgba(220,53,69,1)',
                backgroundColor: 'rgba(220,53,69,0.1)',
                fill: true,
                tension: 0.1,
                pointRadius: 0,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    reverse: true, // y축 반전: 0이 위, 음수/큰값이 아래
                    ticks: {
                        callback: value => formatPercentage(value)
                    }
                }
            },
            plugins: {
                legend: { display: true }
            }
        }
    });
}

// 시계열 차트 생성
function createTimeSeriesChart(chartData) {
    const ctx = document.getElementById('timeSeriesChart').getContext('2d');

    // 기존 차트 제거
    if (timeSeriesChart) {
        timeSeriesChart.destroy();
    }

    const datasets = Object.entries(chartData.series).map(([ticker, data], index) => ({
        label: ticker,
        data: data,
        borderColor: getChartColor(index, 1, ticker),
        backgroundColor: getChartColor(index, 0.1, ticker),
        borderWidth: 3.5,
        fill: false,
        tension: 0.2,
        pointRadius: 0.5,
        pointHoverRadius: 4,
        pointBackgroundColor: getChartColor(index, 1, ticker),
        pointBorderColor: '#ffffff',
        pointBorderWidth: 0.5
    }));

    timeSeriesChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: chartData.dates ? chartData.dates.map(date => formatDate(date)) : [],
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: chartData.title || '누적 수익률 추이'
                },
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                x: {
                    display: true,
                    title: {
                        display: true,
                        text: '날짜'
                    }
                },
                y: {
                    display: true,
                    title: {
                        display: true,
                        text: '누적 수익률 (%)'
                    },
                    ticks: {
                        callback: function (value) {
                            return formatPercentage(value);
                        }
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            }
        }
    });
}

// 비교 차트 생성
function createComparisonChart(chartData) {
    const ctx = document.getElementById('comparisonChart').getContext('2d');

    // 기존 차트 제거
    if (comparisonChart) {
        comparisonChart.destroy();
    }

    // 데이터 구조 확인 및 변환
    let tickers = [];
    let priceReturns = [];
    let totalReturns = [];

    if (chartData.series) {
        // 백엔드에서 제공하는 라벨 정보 사용
        if (chartData.labels && chartData.labels.length > 0) {
            tickers = chartData.labels;
            priceReturns = chartData.series['Price Return'] || [];
            totalReturns = chartData.series['Total Return'] || [];
        } else if (chartData.series['Price Return'] && chartData.series['Total Return']) {
            // 라벨이 없는 경우 기존 로직 사용
            if (typeof chartData.series['Price Return'] === 'object' && !Array.isArray(chartData.series['Price Return'])) {
                tickers = Object.keys(chartData.series['Price Return']);
                priceReturns = Object.values(chartData.series['Price Return']);
                totalReturns = Object.values(chartData.series['Total Return']);
            } else {
                // 배열 형태인 경우 - 라벨 정보가 별도로 있어야 함
                tickers = chartData.labels || [];
                priceReturns = chartData.series['Price Return'] || [];
                totalReturns = chartData.series['Total Return'] || [];
            }
        } else {
            // 다른 구조인 경우 - 기본 처리
            console.warn('Unexpected chart data structure:', chartData);
            tickers = chartData.labels || [];
            priceReturns = [];
            totalReturns = [];
        }
    }

    // 데이터 검증
    if (tickers.length === 0) {
        console.error('No tickers found in chart data');
        return;
    }

    // 디버깅 로그 추가
    console.log('Chart data:', chartData);
    console.log('Tickers:', tickers);
    console.log('Price returns:', priceReturns);
    console.log('Total returns:', totalReturns);

    comparisonChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: tickers,
            datasets: [
                {
                    label: '가격 수익률',
                    data: priceReturns,
                    backgroundColor: 'rgba(54, 162, 235, 0.8)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                },
                {
                    label: '총 수익률',
                    data: totalReturns,
                    backgroundColor: 'rgba(75, 192, 192, 0.8)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: chartData.title || '주식별 수익률 비교'
                },
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                x: {
                    display: true,
                    title: {
                        display: true,
                        text: '주식 심볼'
                    }
                },
                y: {
                    display: true,
                    title: {
                        display: true,
                        text: '수익률 (%)'
                    },
                    ticks: {
                        callback: function (value) {
                            return formatPercentage(value);
                        }
                    }
                }
            }
        }
    });
}

// 개별 주식 분석 테이블 생성
function displayStockAnalysisTable(portfolioData) {
    const tableBody = document.querySelector('#stockAnalysisTable tbody');

    let stocks = [];
    stocks.push(portfolioData.portfolioStockReturn);
    portfolioData.stockReturns.forEach(stock => {
        stocks.push(stock);
    });

    let tableHtml = stocks.map(stock => `
        <tr>
            <td><strong>${stock.ticker}</strong></td>
            <td class="${getReturnClass(stock.priceReturn)}">${formatPercentage(stock.priceReturn)}</td>
            <td class="${getReturnClass(stock.totalReturn)}">${formatPercentage(stock.totalReturn)}</td>
            <td class="${getReturnClass(stock.cagr)}">${formatPercentage(stock.cagr)}</td>
            <td>${formatPercentage(stock.volatility || 0)}</td>
            <td>${formatPercentage(stock.maxDrawdown)}</td>
        </tr>
    `).join('');

    tableBody.innerHTML = tableHtml;
}

// 리스크 지표 표시
function displayRiskMetrics(portfolioData) {
    const riskContainer = document.getElementById('riskMetrics');
    let portfolio = portfolioData.portfolioStockReturn;

    const riskMetrics = [
        {
            label: '샤프 비율',
            value: (portfolio.sharpeRatio || 0).toFixed(2),
            class: 'neutral-return'
        },
        {
            label: '최대 낙폭',
            value: formatPercentage(portfolio.maxDrawdown || 0),
            class: 'negative-return'
        },
        {
            label: 'VaR (95%)',
            value: formatPercentage(portfolio.valueAtRisk || 0),
            class: 'negative-return'
        },
        {
            label: '베타',
            value: (portfolio.beta || 1.0).toFixed(2),
            class: 'neutral-return'
        }
    ];

    riskContainer.innerHTML = riskMetrics.map(metric => `
        <div class="col-md-3 col-sm-6">
            <div class="metric-card">
                <div class="metric-value ${metric.class}">${metric.value}</div>
                <div class="metric-label">${metric.label}</div>
            </div>
        </div>
    `).join('');
}

// 유틸리티 함수들
function formatPercentage(value) {
    return (value * 100).toFixed(2) + '%';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function getReturnClass(value) {
    if (value > 0) return 'positive-return';
    if (value < 0) return 'negative-return';
    return 'neutral-return';
}

function getRecommendationClass(recommendation) {
    const classes = {
        'STRONG_BUY': 'recommendation-strong-buy',
        'BUY': 'recommendation-buy',
        'HOLD': 'recommendation-hold',
        'SELL': 'recommendation-sell',
        'STRONG_SELL': 'recommendation-strong-sell'
    };
    return classes[recommendation] || 'recommendation-hold';
}

function getRecommendationText(recommendation) {
    const texts = {
        'STRONG_BUY': '적극 매수',
        'BUY': '매수',
        'HOLD': '보유',
        'SELL': '매도',
        'STRONG_SELL': '적극 매도'
    };
    return texts[recommendation] || '보유';
}

function getChartColor(index, alpha = 1, label = '') {
    if (label === 'Portfolio' || label === 'Portfolio Total') {
        return `rgba(60, 60, 60, ${alpha})`; // 포트폴리오는 진한 회색
    }
    const colors = [
        `rgba(54, 162, 235, ${alpha})`,   // 파랑
        `rgba(255, 99, 132, ${alpha})`,   // 빨강
        `rgba(75, 192, 192, ${alpha})`,   // 청록
        `rgba(255, 206, 86, ${alpha})`,   // 노랑
        `rgba(153, 102, 255, ${alpha})`,  // 보라
        `rgba(255, 159, 64, ${alpha})`,   // 주황
        `rgba(199, 199, 199, ${alpha})`,  // 회색
        `rgba(83, 102, 255, ${alpha})`    // 인디고
    ];
    return colors[index % colors.length];
}

// UI 상태 관리 함수들
function showLoading() {
    loadingSpinner.style.display = 'block';
}

function hideLoading() {
    loadingSpinner.style.display = 'none';
}

function showResults() {
    resultsSection.style.display = 'block';
    resultsSection.classList.add('fade-in');
}

function hideResults() {
    resultsSection.style.display = 'none';
    resultsSection.classList.remove('fade-in');
}

function showError(message) {
    errorMessage.textContent = message;
    errorSection.style.display = 'block';
}

function hideError() {
    errorSection.style.display = 'none';
}

// 가중치 컨테이너 업데이트
function updateWeightsContainer() {
    const tickersInput = document.getElementById('tickers').value;
    const weightsContainer = document.getElementById('weightsContainer');

    if (!tickersInput.trim()) {
        weightsContainer.innerHTML = `
            <div class="text-muted text-center">
                주식 심볼을 입력하면 가중치 설정이 나타납니다
            </div>
        `;
        return;
    }

    const tickers = tickersInput
        .split(',')
        .map(ticker => ticker.trim().toUpperCase())
        .filter(ticker => ticker.length > 0);

    if (tickers.length === 0) {
        weightsContainer.innerHTML = `
            <div class="text-muted text-center">
                유효한 주식 심볼을 입력해주세요
            </div>
        `;
        return;
    }

    // 기본 가중치 (균등 분배)
    const defaultWeight = (100 / tickers.length).toFixed(1);

    weightsContainer.innerHTML = `
        <div class="row g-2">
            ${tickers.map((ticker, index) => `
                <div class="col-md-6 col-lg-4">
                    <div class="weight-input-group">
                        <label class="form-label fw-bold">${ticker}</label>
                        <div class="input-group">
                            <input type="number" 
                                   class="form-control weight-input" 
                                   id="weight-${ticker}" 
                                   value="${defaultWeight}" 
                                   min="0" 
                                   max="100" 
                                   step="0.1"
                                   data-ticker="${ticker}">
                            <span class="input-group-text">%</span>
                        </div>
                    </div>
                </div>
            `).join('')}
        </div>
        <div class="row mt-3">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>총 가중치: <span id="totalWeight" class="text-primary">100.0%</span></strong>
                    </div>
                    <div>
                        <button type="button" class="btn btn-sm btn-outline-secondary me-2" onclick="equalizeWeights()">
                            <i class="fas fa-balance-scale me-1"></i>균등 분배
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-primary" onclick="normalizeWeights()">
                            <i class="fas fa-calculator me-1"></i>비율 조정
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;

    // 가중치 입력 이벤트 리스너 추가
    document.querySelectorAll('.weight-input').forEach(input => {
        input.addEventListener('input', updateTotalWeight);
        input.addEventListener('change', validateWeights);
    });

    updateTotalWeight();
}

// 총 가중치 업데이트
function updateTotalWeight() {
    const weightInputs = document.querySelectorAll('.weight-input');
    let total = 0;

    weightInputs.forEach(input => {
        const value = parseFloat(input.value) || 0;
        total += value;
    });

    const totalWeightSpan = document.getElementById('totalWeight');
    if (totalWeightSpan) {
        totalWeightSpan.textContent = total.toFixed(1) + '%';

        // 색상 변경
        if (Math.abs(total - 100) < 0.1) {
            totalWeightSpan.className = 'text-success';
        } else if (total > 100) {
            totalWeightSpan.className = 'text-danger';
        } else {
            totalWeightSpan.className = 'text-warning';
        }
    }
}

// 가중치 검증
function validateWeights() {
    const weightInputs = document.querySelectorAll('.weight-input');
    let total = 0;

    weightInputs.forEach(input => {
        const value = parseFloat(input.value) || 0;
        total += value;

        // 개별 입력값 검증
        if (value < 0) {
            input.value = 0;
        } else if (value > 100) {
            input.value = 100;
        }
    });

    updateTotalWeight();
}

// 균등 분배
function equalizeWeights() {
    const weightInputs = document.querySelectorAll('.weight-input');
    if (weightInputs.length === 0) return;

    const equalWeight = (100 / weightInputs.length).toFixed(1);

    weightInputs.forEach(input => {
        input.value = equalWeight;
    });

    updateTotalWeight();
}

// 비율 조정 (총합을 100%로 맞춤)
function normalizeWeights() {
    const weightInputs = document.querySelectorAll('.weight-input');
    if (weightInputs.length === 0) return;

    let total = 0;
    const values = [];

    weightInputs.forEach(input => {
        const value = parseFloat(input.value) || 0;
        values.push(value);
        total += value;
    });

    if (total === 0) {
        equalizeWeights();
        return;
    }

    // 비례적으로 조정
    weightInputs.forEach((input, index) => {
        const normalizedValue = (values[index] / total * 100).toFixed(1);
        input.value = normalizedValue;
    });

    updateTotalWeight();
}

// 가중치 데이터 수집
function getWeightsData() {
    const weightInputs = document.querySelectorAll('.weight-input');
    const weights = [];

    weightInputs.forEach(input => {
        const weight = parseFloat(input.value) || 0;
        weights.push(weight / 100); // 백분율을 소수로 변환
    });

    return weights;
}

// 금액 변화 데이터가 있는지 확인
function hasAmountChanges(portfolioData) {
    // 초기 금액이 설정되어 있는지 확인
    const formData = getFormData();
    return formData.initialAmount && formData.initialAmount > 0;
}

// 금액 변화 차트 생성 (API 데이터 사용)
function createAmountChartFromData(chartData) {
    const ctx = document.getElementById('amountChart').getContext('2d');

    // 기존 차트 제거
    if (amountChart) {
        amountChart.destroy();
    }

    const datasets = Object.entries(chartData.series).map(([ticker, data], index) => ({
        label: ticker,
        data: data,
        borderColor: getChartColor(index),
        backgroundColor: getChartColor(index, 0.1),
        borderWidth: 2,
        fill: false,
        tension: 0.2,
        pointRadius: 1,
        pointHoverRadius: 5,
        pointBackgroundColor: getChartColor(index),
        pointBorderColor: '#ffffff',
        pointBorderWidth: 1
    }));

    amountChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: chartData.dates ? chartData.dates.map(date => formatDate(date)) : [],
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: chartData.title || '포트폴리오 금액 변화'
                },
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: $${context.parsed.y.toLocaleString()}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    display: true,
                    title: {
                        display: true,
                        text: '날짜'
                    }
                },
                y: {
                    display: true,
                    title: {
                        display: true,
                        text: '금액 ($)'
                    },
                    ticks: {
                        callback: function (value) {
                            return '$' + value.toLocaleString();
                        }
                    }
                }
            },
            interaction: {
                intersect: false,
                mode: 'index'
            }
        }
    });
}

// 금액 포맷팅 함수
function formatCurrency(value) {
    return '$' + value.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}
