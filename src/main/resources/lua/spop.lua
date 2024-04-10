function random_delete_bins(rec, num_bins_to_delete)
    if not aerospike:exists(rec) then
        return nil
    end
    local bins = record.bin_names(rec)
    local bin_count = #bins
    if bin_count == 0 then
        return nil
    end
    num_bins_to_delete = math.min(num_bins_to_delete, bin_count)
    local bins_to_delete = {}
    for i = 1, num_bins_to_delete do
        local random_bin_index = math.random(#bins)
        local bin_to_delete = table.remove(bins, random_bin_index)
        rec[bin_to_delete] = nil
        table.insert(bins_to_delete, bin_to_delete)
    end
    aerospike:update(rec)
    local str = table.concat(bins_to_delete, ", ")
    return str
end
