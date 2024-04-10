function delete_bins_return_count(rec, bins_to_delete_str)
    if not aerospike:exists(rec) then
        return 0
    end
    if type(bins_to_delete_str) ~= 'string' then
        error("bins_to_delete must be a string")
    end
    local bins_to_delete = {}
    for bin in string.gmatch(bins_to_delete_str, '([^,]+)') do
        table.insert(bins_to_delete, bin)
    end
    local delete_count = 0
    for i, bin in ipairs(bins_to_delete) do
        if rec[bin] ~= nil then
            rec[bin] = nil
            delete_count = delete_count + 1
        end
    end
    aerospike:update(rec)
    return delete_count
end