var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":223,"id":50,"methods":[{"el":34,"sc":3,"sl":28},{"el":40,"sc":3,"sl":38},{"el":50,"sc":3,"sl":42},{"el":57,"sc":3,"sl":52},{"el":66,"sc":3,"sl":59},{"el":72,"sc":3,"sl":68},{"el":85,"sc":3,"sl":74},{"el":98,"sc":3,"sl":87},{"el":104,"sc":3,"sl":100},{"el":123,"sc":3,"sl":106},{"el":143,"sc":3,"sl":125},{"el":155,"sc":3,"sl":145},{"el":159,"sc":3,"sl":157},{"el":163,"sc":3,"sl":161},{"el":171,"sc":3,"sl":165},{"el":180,"sc":3,"sl":173}],"name":"ExcelFile","sl":26},{"el":222,"id":169,"methods":[{"el":189,"sc":5,"sl":186},{"el":193,"sc":5,"sl":191},{"el":197,"sc":5,"sl":195},{"el":201,"sc":5,"sl":199},{"el":205,"sc":5,"sl":203},{"el":209,"sc":5,"sl":207},{"el":213,"sc":5,"sl":211},{"el":217,"sc":5,"sl":215},{"el":221,"sc":5,"sl":219}],"name":"ExcelFile.ExcelSheet","sl":182}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_2":{"methods":[{"sl":28},{"sl":42},{"sl":52},{"sl":59},{"sl":74},{"sl":87},{"sl":125},{"sl":145},{"sl":157},{"sl":173},{"sl":186},{"sl":191},{"sl":195},{"sl":207}],"name":"testExcelSheet","pass":true,"statements":[{"sl":29},{"sl":30},{"sl":31},{"sl":32},{"sl":33},{"sl":43},{"sl":44},{"sl":45},{"sl":46},{"sl":47},{"sl":48},{"sl":54},{"sl":55},{"sl":56},{"sl":60},{"sl":61},{"sl":62},{"sl":75},{"sl":76},{"sl":77},{"sl":78},{"sl":79},{"sl":80},{"sl":84},{"sl":88},{"sl":89},{"sl":90},{"sl":91},{"sl":93},{"sl":94},{"sl":95},{"sl":97},{"sl":126},{"sl":129},{"sl":130},{"sl":131},{"sl":132},{"sl":133},{"sl":146},{"sl":148},{"sl":150},{"sl":151},{"sl":153},{"sl":158},{"sl":175},{"sl":176},{"sl":187},{"sl":188},{"sl":192},{"sl":196},{"sl":208}]},"test_4":{"methods":[{"sl":42},{"sl":52},{"sl":59},{"sl":74},{"sl":87},{"sl":125},{"sl":145},{"sl":173}],"name":"testExcelFile","pass":true,"statements":[{"sl":43},{"sl":44},{"sl":45},{"sl":46},{"sl":47},{"sl":48},{"sl":54},{"sl":55},{"sl":56},{"sl":60},{"sl":61},{"sl":62},{"sl":75},{"sl":76},{"sl":77},{"sl":78},{"sl":79},{"sl":80},{"sl":84},{"sl":88},{"sl":89},{"sl":90},{"sl":91},{"sl":93},{"sl":94},{"sl":95},{"sl":97},{"sl":126},{"sl":129},{"sl":130},{"sl":131},{"sl":132},{"sl":133},{"sl":146},{"sl":148},{"sl":150},{"sl":151},{"sl":153},{"sl":175},{"sl":176}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [2], [2], [2], [2], [2], [2], [], [], [], [], [], [], [], [], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [], [], [], [2, 4], [], [2, 4], [2, 4], [2, 4], [], [], [2, 4], [2, 4], [2, 4], [2, 4], [], [], [], [], [], [], [], [], [], [], [], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [], [], [], [2, 4], [], [], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [], [2, 4], [2, 4], [2, 4], [], [2, 4], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [2, 4], [2, 4], [], [], [2, 4], [2, 4], [2, 4], [2, 4], [2, 4], [], [], [], [], [], [], [], [], [], [], [], [2, 4], [2, 4], [], [2, 4], [], [2, 4], [2, 4], [], [2, 4], [], [], [], [2], [2], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [2, 4], [], [2, 4], [2, 4], [], [], [], [], [], [], [], [], [], [2], [2], [2], [], [], [2], [2], [], [], [2], [2], [], [], [], [], [], [], [], [], [], [], [2], [2], [], [], [], [], [], [], [], [], [], [], [], [], [], [], []]
