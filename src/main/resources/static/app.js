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
document.addEventListener('DOMContentLoaded', function() {
    portfolioForm.addEventListener('submit', handleFormSubmit);
    
    // 주식 심볼 입력 시 가중치 컨테이너 업데이트
    document.getElementById('tickers').addEventListener('input', updateWeightsContainer);
    
    // 기본 날짜 설정
    const today = new Date();
    const oneYearAgo = new Date(today.getFullYear() - 1, today.getMonth(), today.getDate());
    
    document.getElementById('startDate').value = oneYearAgo.toISOString().split('T')[0];
    document.getElementById('endDate').value = today.toISOString().split('T')[0];
    
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
        const portfolioData = await analyzePortfolio(formData);
        await displayResults(portfolioData);
        showResults();
    } catch (error) {
        console.error('Analysis failed:', error);
        showError(error.message || '포트폴리오 분석 중 오류가 발생했습니다.');
    } finally {
        hideLoading();
    }
}

// 폼 데이터 수집
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
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
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

// 포트폴리오 분석 API 호출
async function analyzePortfolio(formData) {
    const response = await fetch(`${API_BASE_URL}/analyze`, {
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

// 차트 데이터 가져오기
async function getChartData(type, portfolioData) {
    const response = await fetch(`${API_BASE_URL}/chart/${type}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(portfolioData)
    });
    
    if (!response.ok) {
        throw new Error(`차트 데이터를 가져오는데 실패했습니다: ${response.statusText}`);
    }
    
    return await response.json();
}

// 리포트 데이터 가져오기
async function getReportData(portfolioData) {
    const response = await fetch(`${API_BASE_URL}/report`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(portfolioData)
    });
    
    if (!response.ok) {
        throw new Error(`리포트 데이터를 가져오는데 실패했습니다: ${response.statusText}`);
    }
    
    return await response.json();
}

// 결과 표시
async function displayResults(portfolioData) {
    // 포트폴리오 요약 표시
    displayPortfolioSummary(portfolioData);
    
    // 차트 생성
    await createCharts(portfolioData);
    
    // 개별 주식 분석 테이블 생성
    displayStockAnalysisTable(portfolioData);
    
    // 리스크 지표 표시
    displayRiskMetrics(portfolioData);
}

// 포트폴리오 요약 표시
function displayPortfolioSummary(portfolioData) {
    const summaryContainer = document.getElementById('portfolioSummary');
    
    const metrics = [
        {
            label: '포트폴리오 가격 수익률',
            value: formatPercentage(portfolioData.portfolioPriceReturn),
            class: getReturnClass(portfolioData.portfolioPriceReturn)
        },
        {
            label: '포트폴리오 총 수익률',
            value: formatPercentage(portfolioData.portfolioTotalReturn),
            class: getReturnClass(portfolioData.portfolioTotalReturn)
        },
        {
            label: '연평균 성장률 (CAGR)',
            value: formatPercentage(portfolioData.portfolioCAGR),
            class: getReturnClass(portfolioData.portfolioCAGR)
        },
        {
            label: '변동성',
            value: formatPercentage(portfolioData.volatility),
            class: 'neutral-return'
        }
    ];
    
    summaryContainer.innerHTML = metrics.map(metric => `
        <div class="col-md-3 col-sm-6">
            <div class="metric-card">
                <div class="metric-value ${metric.class}">${metric.value}</div>
                <div class="metric-label">${metric.label}</div>
            </div>
        </div>
    `).join('');
}

// 차트 생성
async function createCharts(portfolioData) {
    try {
        // 원본 요청 데이터 가져오기
        const originalRequest = getFormData();
        
        // 시계열 차트 데이터 가져오기
        const timeSeriesData = await getChartData('cumulative', originalRequest);
        createTimeSeriesChart(timeSeriesData);
        
        // 비교 차트 데이터 가져오기
        const comparisonData = await getChartData('comparison', originalRequest);
        createComparisonChart(comparisonData);
        
        // 금액 변화 차트 생성 (초기 금액이 있는 경우)
        if (hasAmountChanges(portfolioData)) {
            const amountData = await getChartData('amount', originalRequest);
            createAmountChartFromData(amountData);
            document.getElementById('amountChartSection').style.display = 'block';
        } else {
            document.getElementById('amountChartSection').style.display = 'none';
        }
        
    } catch (error) {
        console.error('차트 생성 실패:', error);
        // 차트 생성 실패 시 기본 차트 표시
        createDefaultCharts(portfolioData);
    }
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
        borderColor: getChartColor(index),
        backgroundColor: getChartColor(index, 0.1),
        borderWidth: 1.5,
        fill: false,
        tension: 0.2,
        pointRadius: 1.5,
        pointHoverRadius: 5,
        pointBackgroundColor: getChartColor(index),
        pointBorderColor: '#ffffff',
        pointBorderWidth: 1
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
                        callback: function(value) {
                            return formatPercentage(value );
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
                        callback: function(value) {
                            return formatPercentage(value);
                        }
                    }
                }
            }
        }
    });
}

// 기본 차트 생성 (API 실패 시)
function createDefaultCharts(portfolioData) {
    // 시계열 차트
    const timeSeriesCtx = document.getElementById('timeSeriesChart').getContext('2d');
    if (timeSeriesChart) timeSeriesChart.destroy();
    
    const datasets = portfolioData.stockReturns.map((stock, index) => ({
        label: stock.ticker,
        data: stock.cumulativeReturns || [],
        borderColor: getChartColor(index),
        backgroundColor: getChartColor(index, 0.1),
        borderWidth: 2.5,
        fill: false,
        tension: 0.2,
        pointRadius: 0.5,
        pointHoverRadius: 4,
        pointBackgroundColor: getChartColor(index, 0.3),
        pointBorderColor: getChartColor(index),
        pointBorderWidth: 0.5
    }));
    
    timeSeriesChart = new Chart(timeSeriesCtx, {
        type: 'line',
        data: {
            labels: portfolioData.stockReturns[0]?.dates?.map(date => formatDate(date)) || [],
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '누적 수익률 추이'
                }
            }
        }
    });
    
    // 비교 차트
    const comparisonCtx = document.getElementById('comparisonChart').getContext('2d');
    if (comparisonChart) comparisonChart.destroy();
    
    comparisonChart = new Chart(comparisonCtx, {
        type: 'bar',
        data: {
            labels: portfolioData.stockReturns.map(stock => stock.ticker),
            datasets: [
                {
                    label: '가격 수익률',
                    data: portfolioData.stockReturns.map(stock => stock.priceReturn * 100),
                    backgroundColor: 'rgba(54, 162, 235, 0.8)'
                },
                {
                    label: '총 수익률',
                    data: portfolioData.stockReturns.map(stock => stock.totalReturn * 100),
                    backgroundColor: 'rgba(75, 192, 192, 0.8)'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '주식별 수익률 비교'
                }
            }
        }
    });
}

// 개별 주식 분석 테이블 생성
function displayStockAnalysisTable(portfolioData) {
    const tableBody = document.querySelector('#stockAnalysisTable tbody');
    
    tableBody.innerHTML = portfolioData.stockReturns.map(stock => `
        <tr>
            <td><strong>${stock.ticker}</strong></td>
            <td class="${getReturnClass(stock.priceReturn)}">${formatPercentage(stock.priceReturn)}</td>
            <td class="${getReturnClass(stock.totalReturn)}">${formatPercentage(stock.totalReturn)}</td>
            <td class="${getReturnClass(stock.cagr)}">${formatPercentage(stock.cagr)}</td>
            <td>${formatPercentage(stock.volatility || 0)}</td>
            <td><span class="recommendation-badge ${getRecommendationClass(stock.recommendation || 'HOLD')}">${getRecommendationText(stock.recommendation || 'HOLD')}</span></td>
        </tr>
    `).join('');
}

// 리스크 지표 표시
function displayRiskMetrics(portfolioData) {
    const riskContainer = document.getElementById('riskMetrics');
    
    const riskMetrics = [
        {
            label: '샤프 비율',
            value: (portfolioData.sharpeRatio || 0).toFixed(2),
            class: 'neutral-return'
        },
        {
            label: '최대 낙폭',
            value: formatPercentage(portfolioData.maxDrawdown || 0),
            class: 'negative-return'
        },
        {
            label: 'VaR (95%)',
            value: formatPercentage(portfolioData.valueAtRisk || 0),
            class: 'negative-return'
        },
        {
            label: '베타',
            value: (portfolioData.beta || 1.0).toFixed(2),
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

function getChartColor(index, alpha = 1) {
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

// 금액 변화 차트 생성
function createAmountChart(portfolioData) {
    const ctx = document.getElementById('amountChart').getContext('2d');
    
    // 기존 차트 제거
    if (amountChart) {
        amountChart.destroy();
    }
    
    const datasets = portfolioData.stockReturns
        .filter(stock => stock.amountChanges && stock.amountChanges.length > 0)
        .map((stock, index) => ({
            label: stock.ticker,
            data: stock.amountChanges,
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
            labels: portfolioData.stockReturns[0]?.dates?.map(date => formatDate(date)) || [],
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: '포트폴리오 금액 변화'
                },
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
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
                        callback: function(value) {
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
                        label: function(context) {
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
                        callback: function(value) {
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
