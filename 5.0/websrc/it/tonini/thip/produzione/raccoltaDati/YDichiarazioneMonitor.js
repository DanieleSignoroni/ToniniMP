var oldRilevDatiPrdTSOL = RilevDatiPrdTSOL;

RilevDatiPrdTSOL = function() {
	oldRilevDatiPrdTSOL();
	$(function() {
		var table = $('#extraTable');

		if (!table.length) return;
		if ($.fn.DataTable.isDataTable(table)) return;

		table.DataTable({
			pageLength: 25,
			stateSave: true,
			stateLoadParams: function(settings, data) {
				data.order = [[2, 'asc'], [8, 'asc']];
			},
			order: [[1, 'asc'], [7, 'asc']],
			autoWidth: false,
			language: { search: "Filtra:", zeroRecords: "Nessun risultato" }
		});
	});
}